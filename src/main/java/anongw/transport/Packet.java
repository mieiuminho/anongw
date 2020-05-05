package anongw.transport;

import anongw.util.Converter;
import anongw.util.Encoder;

import java.io.IOException;
import java.io.Serializable;

public final class Packet implements Serializable {

    public enum TYPE implements Serializable {
        REQUEST, RESPONSE
    };

    private TYPE type;

    private String gateway;
    private int session;
    private int part;

    private byte[] content;

    public Packet(final TYPE type, final String gateway, final int session, final int part, final byte[] content) {
        this.type = type;
        this.gateway = gateway;
        this.session = session;
        this.part = part;
        this.content = content;
    }

    public TYPE getType() {
        return type;
    }

    public String getGateway() {
        return gateway;
    }

    public int getSession() {
        return session;
    }

    public int getPart() {
        return part;
    }

    public byte[] getContent() {
        return content;
    }

    public byte[] encode() throws IOException {
        return Encoder.toByteArray(this);
    }

    public static Packet decode(final byte[] encoded) throws IOException, ClassNotFoundException {
        return (Packet) Encoder.fromByteArray(encoded);
    }

    @Override
    public String toString() {
        return "Packet{" + "type=" + type + ", gateway='" + gateway + "'" + ", session=" + session + ", part=" + part
                + ", content=\n" + Converter.fromBytes(content).toString() + "\n}";
    }
}
