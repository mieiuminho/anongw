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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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

public final class Distributor implements Runnable {
    private static Logger log = LogManager.getLogger(Distributor.class);

    /**
     * TCP port
     */
    private int tcp;
    /**
     * Target Server Address
     */
    private String destination;

    /**
     * UDP port
     */
    private int udp;
    /**
     * Gateway address
     */
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

    public static final class Builder {
        private int tcp;
        private String destination;

        private int udp;
        private String address;

        private BlockingQueue<byte[]> packets;
        private Map<Integer, PacketsQueue> responses;

        private Map<Integer, Set<Integer>> acks;
        private Map<Integer, Map<Integer, Packet>> pendingacks;

        private Map<Integer, Map<Integer, String>> peers;

        public Builder tpc(final int port) {
            this.tcp = port;
            return this;
        }

        public Builder destination(final String ip) {
            this.destination = ip;
            return this;
        }

        public Builder udp(final int port) {
            this.udp = port;
            return this;
        }

        public Builder address(final String ip) {
            this.address = ip;
            return this;
        }

        public Builder packets(final BlockingQueue<byte[]> list) {
            this.packets = list;
            return this;
        }

        public Builder responses(final Map<Integer, PacketsQueue> received) {
            this.responses = received;
            return this;
        }

        public Builder acks(final Map<Integer, Set<Integer>> confirmations) {
            this.acks = confirmations;
            return this;
        }

        public Builder pending(final Map<Integer, Map<Integer, Packet>> sent) {
            this.pendingacks = sent;
            return this;
        }

        public Builder peers(final Map<Integer, Map<Integer, String>> nodes) {
            this.peers = nodes;
            return this;
        }

        public Distributor build() {
            return new Distributor(this);
        }
    }

    private Distributor(final Builder builder) {
        this.tcp = builder.tcp;
        this.destination = builder.destination;
        this.udp = builder.udp;
        this.address = builder.address;
        this.packets = builder.packets;
        this.requests = new ConcurrentHashMap<>();
        this.responses = builder.responses;
        this.acks = builder.acks;
        this.pendingacks = builder.pendingacks;
        this.peers = builder.peers;
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

                switch (packet.getType()) {
                    case REQUEST :
                        // Caso em que é a primeira vez que se recebe um pacote deste gateway
                        if (!this.requests.containsKey(packet.getGateway())) {
                            this.requests.put(packet.getGateway(), new ConcurrentHashMap<>());
                        }

                        // Caso em que é a primeira vez que se recebe um pacote deste gateway com aquele número de
                        // sessão
                        if (!this.requests.get(packet.getGateway()).containsKey(packet.getSession())) {
                            // ligação ao servidor
                            Socket target = new Socket(this.destination, this.tcp);
                            DataOutputStream out = new DataOutputStream(target.getOutputStream());

                            // lista onde vao estar todos os pedidos desta sessão deste cliente
                            PacketsQueue queue = new PacketsQueue();

                            // thread que vai escrever para o servidor
                            new Thread(new ConnectionWriter(this.address, queue, out)).start();

                            // thread que vai ler do servidor e enviar os pacotes de volta para o cliente
                            new Thread(new ConnectionReader(Packet.TYPE.RESPONSE, packet.getSession(), this.address,
                                    target, packet.getGateway(), this.udp, this.pendingacks, this.peers, this.acks))
                                            .start();

                            // adicionar a lista ao map do distributor
                            this.requests.get(packet.getGateway()).put(packet.getSession(), queue);
                        }

                        this.requests.get(packet.getGateway()).get(packet.getSession()).put(packet);
                        sendACK(packet);
                        break;

                    case RESPONSE :
                        this.responses.get(packet.getSession()).put(packet);
                        sendACK(packet);
                        break;

                    case ACK :
                        if (!this.acks.containsKey(packet.getSession())) {
                            this.acks.put(packet.getSession(), new HashSet<>());
                        }

                        this.acks.get(packet.getSession()).add(packet.getPart());
                        break;
                    default :
                        log.error("Invalid packet type: " + packet.getType());
                }
            } catch (IOException | InterruptedException | NoSuchAlgorithmException | ClassNotFoundException
                    | InvalidKeyException | SignatureException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
