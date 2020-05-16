package anongw.server;

import anongw.concurrent.PacketsQueue;
import anongw.transport.Packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;

public final class ConnectionWriter implements Runnable {
    private static Logger log = LogManager.getLogger(ConnectionWriter.class);

    private PacketsQueue messages;

    private DataOutputStream out;

    public ConnectionWriter(final PacketsQueue messages, final DataOutputStream out) {
        this.messages = messages;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            // Socket target = new Socket(this.destination, this.tcp);
            // BufferedReader in = new BufferedReader(new InputStreamReader(target.getInputStream()));
            // PrintWriter out = new PrintWriter(target.getOutputStream(), true);

            while (true) {
                // TODO garantir ordem antes de enviar
                Packet packet = this.messages.take();
                log.debug("Enviar pacote para target server=" + packet.getContent());
                this.out.write(packet.getContent());
            }

        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
