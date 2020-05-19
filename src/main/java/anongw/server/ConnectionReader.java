package anongw.server;

import anongw.common.Config;
import anongw.security.Encryption;
import anongw.transport.Packet;
import anongw.util.Encoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads from a TCP connection and sends the result and sends it through a UDP socket
 */

public final class ConnectionReader implements Runnable {
    private static Logger log = LogManager.getLogger(ConnectionReader.class);

    private int id;
    private Packet.TYPE type;

    private String address;
    private final Socket client;
    private DataInputStream in;

    private String peer;
    private int udp;
    private DatagramSocket out;
    private byte[] buffer;

    private Map<Integer, Map<Integer, Packet>> pendingAcks;
    private Map<Integer, Map<Integer, String>> peers;
    private Map<Integer, Set<Integer>> acks;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public ConnectionReader(final Packet.TYPE type, final int id, final String address, final Socket client,
            final String peer, final int udp, final Map<Integer, Map<Integer, Packet>> pendingAcks,
            final Map<Integer, Map<Integer, String>> peers, final Map<Integer, Set<Integer>> acks) {
        this.type = type;
        this.id = id;
        this.address = address;
        this.client = client;
        this.udp = udp;
        this.peer = peer;
        this.buffer = new byte[Config.BUFFER_SIZE];
        this.pendingAcks = pendingAcks;
        this.peers = peers;
        this.acks = acks;
    }

    /**
     *
     * @return PrivateKey of this gateway
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public PrivateKey getPrivateKey() throws IOException, ClassNotFoundException {
        return (PrivateKey) Encoder.fromFile(Config.KEYS_DIR + this.address + ".key");
    }

    /**
     *
     * @return PublicKey of the destination gateway
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public PublicKey getPublicKey() throws IOException, ClassNotFoundException {
        return (PublicKey) Encoder.fromFile(Config.KEYS_DIR + this.peer + ".pub");
    }

    @Override
    public void run() {
        log.info("Connection " + this.id + " established with on " + this.client.getRemoteSocketAddress());

        try {
            this.in = new DataInputStream(this.client.getInputStream());
            this.out = new DatagramSocket();

            int part = 1;

            while (this.in.read(buffer, 0, buffer.length) != -1) {
                byte[] encrypted = Encryption.encrypt(this.getPublicKey(), buffer);
                Packet packet = new Packet(this.type, this.address, this.id, part++, encrypted,
                        Encryption.sign(encrypted, this.getPrivateKey()));

                byte[] packetBytes = packet.encode();
                this.out.send(
                        new DatagramPacket(packetBytes, packetBytes.length, InetAddress.getByName(peer), this.udp));

                if (!this.pendingAcks.containsKey(this.id)) {
                    this.pendingAcks.put(this.id, new ConcurrentHashMap<>());
                    this.peers.put(this.id, new ConcurrentHashMap<>());
                }

                this.pendingAcks.get(this.id).put(part - 1, packet);
                this.peers.get(this.id).put(part - 1, peer);

            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException
                | SignatureException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            log.error(e.getMessage(), e);
        }

        log.info("Connection " + this.id + " finished on " + this.client.getRemoteSocketAddress());
    }
}
