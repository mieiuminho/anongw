package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public final class AnonGW {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    private static final String HOSTNAME = "127.0.0.1";

    private int port;
    private ServerSocket socket;

    private String targetServerAddress;
    private Socket target;

    private List<String> overlayPeersAddresses;

    public AnonGW() {
    }

    public AnonGW(final String targetServerAddress, final int port, final List<String> overlayPeersAddresses) {
        this.targetServerAddress = targetServerAddress;
        this.port = port;
        this.overlayPeersAddresses = overlayPeersAddresses;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void startUp() {
        log.debug("Working Directory " + System.getProperty("user.dir"));

        try {
            this.socket = new ServerSocket();
            this.socket.bind(new InetSocketAddress(HOSTNAME, this.port));
            log.info("Server is up at " + this.socket.getLocalSocketAddress());
            this.target = new Socket(targetServerAddress, 9000); // changing port temporally for testing purposes
                                                                 // (because we cannot use the same port for anongw and
                                                                 // target server in same network) it should be `port`
            log.info("Connected to target server at " + this.target.getLocalSocketAddress());
        } catch (IOException e) {
            log.fatal(e.getMessage());
            e.printStackTrace();
        }

        int id = 1;
        while (true) {
            try {
                log.info("Waiting for connection...");
                Socket client = this.socket.accept();
                new Thread(new Session(id, client, this.target)).start();
                log.debug("Session " + id + " accepted connection");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            id++;
        }
    }
}
