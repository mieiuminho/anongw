package anongw.concurrent;

import anongw.server.ConnectionReader;
import anongw.server.ConnectionWriter;
import anongw.transport.Packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public final class Distributor implements Runnable {
    private static Logger log = LogManager.getLogger(Distributor.class);

    private int tcp;
    private String destination;

    private int udp;
    private String address;

    private BlockingQueue<byte[]> packets;

    // gateways -> sessions -> [parts] (pacotes destinados ao target server)
    private Map<String, Map<Integer, PacketsQueue>> requests;

    // sessions -> [parts] (pacotes de volta ao cliente)
    private Map<Integer, PacketsQueue> responses;

    public Distributor(final BlockingQueue<byte[]> packets, final Map<Integer, PacketsQueue> responses, final int tcp,
            final String destination, final int udp, final String address) {
        this.tcp = tcp;
        this.destination = destination;
        this.udp = udp;
        this.address = address;
        this.packets = packets;
        this.requests = new ConcurrentHashMap<>();
        this.responses = responses;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Packet packet = Packet.decode(this.packets.take());
                log.debug("received packet = " + packet.toString());

                if (packet.getType() == Packet.TYPE.REQUEST) {
                    // Caso em que é a primeira vez que se recebe um pacote deste gateway
                    if (!this.requests.containsKey(packet.getGateway())) {
                        this.requests.put(packet.getGateway(), new ConcurrentHashMap<>());
                    }

                    // Caso em que é a primeira vez que se recebe um pacote deste gateway com aquele número de sessão
                    if (!this.requests.get(packet.getGateway()).containsKey(packet.getSession())) {
                        // ligação ao servidor
                        Socket target = new Socket(this.destination, this.tcp);
                        DataOutputStream out = new DataOutputStream(target.getOutputStream());

                        // lista onde vao estar todos os pedidos desta sessão deste cliente
                        PacketsQueue queue = new PacketsQueue();

                        // thread que vai escrever para o servidor
                        new Thread(new ConnectionWriter(queue, out)).start();

                        // thread que vai ler do servidor e enviar os pacotes de volta para o cliente
                        new Thread(new ConnectionReader(Packet.TYPE.RESPONSE, packet.getSession(), this.address, target,
                                packet.getGateway(), this.udp)).start();

                        // adicionar a lista ao map do distributor
                        this.requests.get(packet.getGateway()).put(packet.getSession(), queue);
                    }

                    this.requests.get(packet.getGateway()).get(packet.getSession()).put(packet);
                } else if (packet.getType() == Packet.TYPE.RESPONSE) {
                    this.responses.get(packet.getSession()).put(packet);
                }

            } catch (InterruptedException | IOException | ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
