package anongw.server;

import anongw.common.Config;
import anongw.concurrent.PacketsQueue;
import anongw.security.Encryption;
import anongw.transport.Packet;

import anongw.util.Encoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public final class ConnectionWriter implements Runnable {
    private static Logger log = LogManager.getLogger(ConnectionWriter.class);

    private String gateway;

    private PacketsQueue messages;

    private DataOutputStream out;

    public ConnectionWriter(final String gateway, final PacketsQueue messages, final DataOutputStream out) {
        this.gateway = gateway;
        this.messages = messages;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Packet packet = this.messages.take();
                log.debug("Enviar pacote para target server=" + packet.getContent());
                this.out.write(Encryption.decrypt(
                        (PrivateKey) Encoder.fromFile(Config.KEYS_DIR + this.gateway + ".key"), packet.getContent()));
            }

        } catch (InterruptedException | IOException | ClassNotFoundException | NoSuchPaddingException
                | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            log.error(e.getMessage(), e);
        }
    }
}
