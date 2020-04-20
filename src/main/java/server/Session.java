package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import transport.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Session implements Runnable {
    private static Logger log = LogManager.getLogger(AnonGW.class);

    private int id;

    private final Socket client;
    private BufferedReader clientIn;
    private PrintWriter clientOut;

    private final Socket target;
    private BufferedReader targetIn;
    private PrintWriter targetOut;

    public Session(final int id, final Socket client, final Socket target) {
        this.id = id;
        this.client = client;
        this.target = target;
    }

    @Override
    public final void run() {
        log.info("Session " + this.id + " established on " + this.client.getRemoteSocketAddress());

        try {
            this.clientIn = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.clientOut = new PrintWriter(this.client.getOutputStream(), true);

            this.targetIn = new BufferedReader(new InputStreamReader(this.target.getInputStream()));
            this.targetOut = new PrintWriter(this.target.getOutputStream(), true);

            new Thread(new Connection(this.id, this.clientIn, this.targetOut)).start();
            new Thread(new Connection(this.id, this.targetIn, this.clientOut)).start();

        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // try {
        // this.client.shutdownOutput();
        // this.client.shutdownInput();
        // this.client.close();
        //
        // log.info("Session " + this.id + " finished on " + this.client.getRemoteSocketAddress());
        // } catch (IOException e) {
        // log.error(e.getMessage());
        // }
    }
}
