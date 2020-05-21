package anongw.common;

public final class Config {
    public static final int BUFFER_SIZE = 245; // Max size allowed by RSACipher
    public static final int DATAGRAM_MAX_SIZE = 2048;
    public static final String KEYS_DIR = "src/main/resources/keys/";
    public static final int TIMEOUT = 1000;

    private Config() {}
}
