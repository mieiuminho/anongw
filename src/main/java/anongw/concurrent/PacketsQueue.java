package anongw.concurrent;

import anongw.transport.Packet;
import java.util.concurrent.PriorityBlockingQueue;

public final class PacketsQueue {
    private PriorityBlockingQueue<Packet> packets = new PriorityBlockingQueue<>();
    private int next = 1;

    public synchronized void put(final Packet packet) {
        this.packets.put(packet);
        this.notifyAll();
    }

    public synchronized Packet take() throws InterruptedException {
        if (this.packets.peek() == null || this.packets.peek().getPart() != next) this.wait();
        this.next++;
        return this.packets.take();
    }
}
