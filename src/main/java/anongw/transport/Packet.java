package anongw.transport;

import anongw.util.Converter;
import anongw.util.Encoder;

import java.io.IOException;
import java.io.Serializable;

public final class Packet implements Serializable, Comparable<Packet> {

    @Override
    public int compareTo(final Packet packet) {
        return Integer.compare(this.getPart(), packet.getPart());
    }

    public enum TYPE implements Serializable {
        REQUEST, RESPONSE
    };

    private TYPE type;

    private String gateway;
    private int session;
    private int part;

    private byte[] content;

    private String signature;

    public Packet(final TYPE type, final String gateway, final int session, final int part, final byte[] content,
            final String signature) {
        this.type = type;
        this.gateway = gateway;
        this.session = session;
        this.part = part;
        this.content = content;
        this.signature = signature;
    }

    public TYPE getType() {
        return this.type;
    }

    public String getGateway() {
        return this.gateway;
    }

    public int getSession() {
        return this.session;
    }

    public int getPart() {
        return this.part;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getSignature() {
        return this.signature;
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
