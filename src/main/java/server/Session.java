package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Session implements Runnable {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    private int id;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Session(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    @Override
    public void run() {
        log.info("Session " + this.id + " established on " + this.socket.getRemoteSocketAddress());

        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                this.out.println(message);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        try {
            this.socket.shutdownOutput();
            this.socket.shutdownInput();
            this.socket.close();

            log.info("Session " + this.id + " finished on " + this.socket.getRemoteSocketAddress());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
