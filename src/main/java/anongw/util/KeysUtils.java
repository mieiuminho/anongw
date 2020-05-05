package anongw.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;

public final class KeysUtils {

    private static KeyPairGenerator keyGen;
    @SuppressWarnings("checkstyle:MagicNumber")
    private static final int KEY_SIZE = 2048;

    static {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KEY_SIZE, random);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private KeysUtils() {
    }

    private static void toFile(final String name, final String extension, final Object obj) throws IOException {
        String fileName = name + "." + extension;
        FileOutputStream fout = new FileOutputStream(fileName);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(obj);
        oout.flush();
        oout.close();
    }

    public static void generate(final Collection<String> names) {
        names.forEach(KeysUtils::generate);
    }

    public static void generate(final String s) {
        try {
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey pub = keyPair.getPublic();
            PrivateKey priv = keyPair.getPrivate();
            KeysUtils.toFile(s, "pub", pub);
            KeysUtils.toFile(s, "key", priv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
