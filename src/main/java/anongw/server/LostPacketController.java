package anongw.server;

import anongw.common.Config;
import anongw.transport.Packet;
import anongw.util.Converter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LostPacketController implements Runnable {
    private static Logger log = LogManager.getLogger(LostPacketController.class);

    /** UDP port */
    private int udp;

    private DatagramSocket out;

    private Map<Integer, Map<Integer, String>> peers;
    private Map<Integer, Map<Integer, Packet>> pendingAcks;
    private Map<Integer, Set<Integer>> acks;

    public LostPacketController(
            final int udp,
            final Map<Integer, Map<Integer, Packet>> pendingAcks,
            final Map<Integer, Map<Integer, String>> peers,
            final Map<Integer, Set<Integer>> acks) {
        this.udp = udp;
        this.pendingAcks = pendingAcks;
        this.peers = peers;
        this.acks = acks;
    }

    private void resend(final Packet p) throws IOException {
        String peer = this.peers.get(p.getSession()).get(p.getPart());
        byte[] packetBytes = Converter.compress(p.encode());
        this.out.send(
                new DatagramPacket(
                        packetBytes, packetBytes.length, InetAddress.getByName(peer), this.udp));
    }

    private boolean ackReceived(final Packet packet) {
        return this.acks.containsKey(packet.getSession())
                && this.acks.get(packet.getSession()).contains(packet.getPart());
    }

    @Override
    public void run() {
        try {
            this.out = new DatagramSocket();

            while (true) {
                for (Map<Integer, Packet> sessionPackets : pendingAcks.values()) {
                    for (Packet packet : sessionPackets.values()) {
                        if (ackReceived(packet)) {
                            this.pendingAcks.get(packet.getSession()).remove(packet.getPart());
                            this.peers.get(packet.getSession()).remove(packet.getPart());
                        } else {
                            this.resend(packet);
                        }
                    }
                }

                Thread.sleep(Config.TIMEOUT);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
