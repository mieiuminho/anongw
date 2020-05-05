package anongw.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class Encoder {

    private Encoder() {
    }

    public static Object fromByteArray(final byte[] encoded) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(encoded)));
        Object object = stream.readObject();
        stream.close();

        return object;
    }

    public static byte[] toByteArray(final Serializable object) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(data));
        stream.flush();
        stream.writeObject(object);
        stream.flush();
        stream.close();

        return data.toByteArray();
    }
}
