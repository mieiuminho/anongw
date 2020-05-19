package anongw.concurrent;

import anongw.common.Config;
import anongw.security.Encryption;
import anongw.server.ConnectionReader;
import anongw.server.ConnectionWriter;
import anongw.transport.Packet;
import anongw.util.Encoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.security.SecureRandom;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:AvoidStarImport"})
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
    // acks já recebidos
    private Map<Integer, Set<Integer>> acks;
    // Packets pending ack
    private Map<Integer, Map<Integer, Packet>> pendingacks;

    private Map<Integer, Map<Integer, String>> peers;

    public Distributor(final BlockingQueue<byte[]> packets, final Map<Integer, PacketsQueue> responses,
            final Map<Integer, Set<Integer>> acks, final Map<Integer, Map<Integer, Packet>> pendingacks,
            final Map<Integer, Map<Integer, String>> peers, final int tcp, final String destination, final int udp,
            final String address) {
        this.tcp = tcp;
        this.destination = destination;
        this.udp = udp;
        this.address = address;
        this.packets = packets;
        this.requests = new ConcurrentHashMap<>();
        this.responses = responses;
        this.acks = acks;
        this.pendingacks = pendingacks;
        this.peers = peers;
    }

    private void sendACK(final Packet packet) {
        try {
            DatagramSocket tunnel = new DatagramSocket();
            PrivateKey key = (PrivateKey) Encoder.fromFile(Config.KEYS_DIR + this.address + ".key");
            byte[] content = new byte[1];
            new SecureRandom().nextBytes(content);

            byte[] ack = new Packet(Packet.TYPE.ACK, this.address, packet.getSession(), packet.getPart(), content,
                    Encryption.sign(content, key)).encode();

            tunnel.send(new DatagramPacket(ack, ack.length, InetAddress.getByName(packet.getGateway()), this.udp));

            tunnel.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Packet packet = Packet.decode(this.packets.take());
                log.debug("received packet = " + packet.toString());

                // verify if the package is really from the gateway host, if not it is discarded
                if (!Encryption.verify(packet.getSignature(), packet.getContent(),
                        (PublicKey) Encoder.fromFile(Config.KEYS_DIR + packet.getGateway() + ".pub"))) {
                    log.warn("Discarding fake packet from fake Gateway");
                    continue;
                }

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
                        new Thread(new ConnectionWriter(this.address, queue, out)).start();

                        // thread que vai ler do servidor e enviar os pacotes de volta para o cliente
                        new Thread(new ConnectionReader(Packet.TYPE.RESPONSE, packet.getSession(), this.address, target,
                                packet.getGateway(), this.udp, this.pendingacks, this.peers, this.acks)).start();

                        // adicionar a lista ao map do distributor
                        this.requests.get(packet.getGateway()).put(packet.getSession(), queue);
                    }

                    this.requests.get(packet.getGateway()).get(packet.getSession()).put(packet);
                    sendACK(packet);

                } else if (packet.getType() == Packet.TYPE.RESPONSE) {
                    this.responses.get(packet.getSession()).put(packet);
                    sendACK(packet);

                } else if (packet.getType() == Packet.TYPE.ACK) {
                    int session = packet.getSession();
                    int part = packet.getPart();

                    if (!this.acks.containsKey(session)) {
                        this.acks.put(session, new HashSet<>());
                    }
                    this.acks.get(session).add(part);

                }

            } catch (IOException | InterruptedException | NoSuchAlgorithmException | ClassNotFoundException
                    | InvalidKeyException | SignatureException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
