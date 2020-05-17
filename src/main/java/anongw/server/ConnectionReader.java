package anongw.server;

import anongw.common.Config;
import anongw.security.Encryption;
import anongw.transport.Packet;
import anongw.util.Converter;
import anongw.util.Encoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

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

    public ConnectionReader(final Packet.TYPE type, final int id, final String address, final Socket client,
            final String peer, final int udp) {
        this.type = type;
        this.id = id;
        this.address = address;
        this.client = client;
        this.udp = udp;
        this.peer = peer;
        this.buffer = new byte[Config.BUFFER_SIZE];
    }

    public PrivateKey getPrivateKey() throws IOException, ClassNotFoundException {
        return (PrivateKey) Encoder.fromFile("keys/" + this.address + ".key");
    }

    @Override
    public void run() {
        log.info("Connection " + this.id + " established with on " + this.client.getRemoteSocketAddress());

        try {
            this.in = new DataInputStream(this.client.getInputStream());
            this.out = new DatagramSocket();

            int part = 1;

            while (this.in.read(buffer, 0, buffer.length) != -1) {
                byte[] packet = new Packet(this.type, this.address, this.id, part++, buffer,
                        Encryption.sign(buffer, this.getPrivateKey())).encode();
                log.debug("Sending data: " + Converter.fromBytes(buffer).toString());
                this.out.send(new DatagramPacket(packet, packet.length, InetAddress.getByName(peer), this.udp));
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            log.error(e.getMessage(), e);
        }

        log.info("Connection " + this.id + " finished on " + this.client.getRemoteSocketAddress());
    }
}
