package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class AnonGW {
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 8080;

    private static Logger log = LogManager.getLogger(AnonGW.class);

    private ServerSocket socket;

    public AnonGW() {
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void startUp() {
        log.debug("Working Directory " + System.getProperty("user.dir"));

        try {
            this.socket = new ServerSocket();
            this.socket.bind(new InetSocketAddress(HOSTNAME, PORT));
            log.info("Server is up at " + this.socket.getLocalSocketAddress());
        } catch (IOException e) {
            log.fatal(e.getMessage());
            e.printStackTrace();
        }

        int id = 0;
        while (true) {
            try {
                log.info("Waiting for connection...");
                Socket clientServer = this.socket.accept();
                new Thread(new Session(id, clientServer)).start();
                log.debug("Session " + id + " accepted connection");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            id++;
        }
    }
}
