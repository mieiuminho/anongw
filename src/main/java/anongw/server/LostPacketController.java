package anongw.server;

import anongw.transport.Packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
@SuppressWarnings({"checkstyle:ParameterNumber", "checkstyle:MagicNumber"})
public final class LostPacketController implements Runnable {
    private static Logger log = LogManager.getLogger(LostPacketController.class);

    private int timeout = 1000;
    private DatagramSocket out;
    private int udp;

    private Map<Integer, Map<Integer, String>> peers;
    private Map<Integer, Map<Integer, Packet>> pendingAcks;
    private Map<Integer, Set<Integer>> acks;

    public LostPacketController(final int udp, final Map<Integer, Map<Integer, Packet>> pendingAcks,
            final Map<Integer, Map<Integer, String>> peers, final Map<Integer, Set<Integer>> acks)
            throws SocketException {
        this.out = new DatagramSocket();
        this.udp = udp;
        this.pendingAcks = pendingAcks;
        this.peers = peers;
        this.acks = acks;
    }

    private void resend(final Packet p) throws IOException {
        String peer = this.peers.get(p.getSession()).get(p.getPart());
        byte[] packetBytes = p.encode();
        this.out.send(new DatagramPacket(packetBytes, packetBytes.length, InetAddress.getByName(peer), this.udp));
    }

    private boolean ackReceived(final Packet p) {
        int id = p.getSession();
        int part = p.getPart();
        return (this.acks.containsKey(id) && this.acks.get(id).contains(part));
    }

    @Override
    public void run() {
        try {
            while (true) {
                for (Map<Integer, Packet> sessionPackets : pendingAcks.values()) {
                    for (Packet p : sessionPackets.values()) {
                        if (!ackReceived(p)) {
                            resend(p);
                        } else {
                            this.pendingAcks.get(p.getSession()).remove(p.getPart());
                            this.peers.get(p.getSession()).remove(p.getPart());
                        }
                    }
                }
                Thread.sleep(timeout);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
