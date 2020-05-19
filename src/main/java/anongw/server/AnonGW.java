package anongw.server;

import anongw.common.Config;
import anongw.concurrent.Distributor;
import anongw.concurrent.PacketsQueue;
import anongw.transport.Packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public final class AnonGW {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    private String hostname;

    private int tcp;
    private ServerSocket connection;

    private int udp;
    private DatagramSocket tunnel;

    private String destination;

    private List<String> peers;

    private BlockingQueue<byte[]> packets;

    // mensagens que vêm de volta por udp para o cliente
    private Map<Integer, PacketsQueue> responses;
    // acks received
    private Map<Integer, Set<Integer>> acks;
    // packets sent but waiting for the ack from destination
    private Map<Integer, Map<Integer, Packet>> pendingAcks;
    //
    private Map<Integer, Map<Integer, String>> destinations;

    private AnonGW() {
    }

    public AnonGW(final String destination, final String hostname, final int tcp, final int udp,
            final List<String> peers) {
        this.destination = destination;
        this.hostname = hostname;
        this.tcp = tcp;
        this.udp = udp;
        this.peers = peers;
        this.packets = new LinkedBlockingQueue<>();
        this.responses = new ConcurrentHashMap<>();
        this.acks = new ConcurrentHashMap<>();
        this.pendingAcks = new ConcurrentHashMap<>();
        this.destinations = new ConcurrentHashMap<>();
    }

    public void startUp() {
        log.debug("Working Directory " + System.getProperty("user.dir"));

        new Thread(new Distributor(this.packets, this.responses, this.acks, this.pendingAcks, this.destinations,
                this.tcp, this.destination, this.udp, this.hostname)).start();

        try {
            new Thread(new LostPacketController(this.udp, this.pendingAcks, this.destinations, this.acks)).start();
            this.connection = new ServerSocket();
            this.connection.bind(new InetSocketAddress(this.hostname, this.tcp));
            log.info("Server (TCP) is up at " + this.connection.getLocalSocketAddress());

            this.tunnel = new DatagramSocket(this.udp, InetAddress.getByName(this.hostname));
            log.info("Server (UPD) is up at " + this.tunnel.getLocalSocketAddress());
        } catch (IOException e) {
            log.fatal(e.getMessage(), e);
        }

        this.listen();
    }

    private void listen() {
        // Open a new tcp connection for each client and create a new session for each
        new Thread(new Runnable() {
            private int id = 1;

            @Override
            public void run() {
                while (true) {
                    try {
                        log.info("(TCP) Waiting for connection...");
                        Socket client = connection.accept();

                        // Thread que vai ler do cliente
                        new Thread(new ConnectionReader(Packet.TYPE.REQUEST, id, hostname, client,
                                peers.get(new Random().nextInt(peers.size())), udp, pendingAcks, destinations, acks))
                                        .start();

                        PacketsQueue messages = new PacketsQueue();
                        responses.put(id, messages);
                        // Thread que vai escrever para o cliente
                        new Thread(new ConnectionWriter(hostname, messages,
                                new DataOutputStream(client.getOutputStream()))).start();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    this.id++;
                }
            }
        }).start();

        // Store each udp packet for later processing in different threads
        new Thread(new Runnable() {
            private byte[] buffer = new byte[Config.DATAGRAM_MAX_SIZE];

            @Override
            public void run() {
                while (true) {
                    try {
                        log.info("(UDP) Waiting for packets...");
                        tunnel.receive(new DatagramPacket(this.buffer, this.buffer.length));
                        packets.put(this.buffer);
                        // Clear the buffer after every message.
                        this.buffer = new byte[Config.DATAGRAM_MAX_SIZE];
                    } catch (IOException | InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }).start();
    }
}
