package transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public final class Connection implements Runnable {
    private static Logger log = LogManager.getLogger(Connection.class);

    private int id;
    private BufferedReader in;
    private PrintWriter out;

    public Connection(final int id, final BufferedReader in, final PrintWriter out) {
        this.id = id;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = this.in.readLine()) != null) {
                this.out.println(message);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
