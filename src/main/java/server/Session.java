package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import transport.Tunnel;

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
        log.info("Session " + this.id + " established with client on " + this.client.getRemoteSocketAddress());
        log.info("Session " + this.id + " established with target server on " + this.target.getRemoteSocketAddress());

        try {
            this.clientIn = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
            this.clientOut = new PrintWriter(this.client.getOutputStream(), true);

            this.targetIn = new BufferedReader(new InputStreamReader(this.target.getInputStream()));
            this.targetOut = new PrintWriter(this.target.getOutputStream(), true);

            Thread[] threads = {//
                    new Thread(new Tunnel(this.id, this.clientIn, this.targetOut)), // requests
                    new Thread(new Tunnel(this.id, this.targetIn, this.clientOut)), // responses
            };

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }

        log.info("Session " + this.id + " finished on " + this.client.getRemoteSocketAddress());
    }
}
