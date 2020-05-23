package anongw.util;

import anongw.common.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class Converter {

    private Converter() {}

    public static byte[] compress(final byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[Config.DATAGRAM_MAX_SIZE];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public static byte[] decompress(final byte[] data) throws DataFormatException, IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[Config.DATAGRAM_MAX_SIZE];

        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * A utility method to convert the byte array data into a string representation.
     *
     * @param data the array of bytes
     * @return null if the array is null
     */
    public static String fromBytes(final byte[] data) {
        if (data == null) return null;

        StringBuilder ret = new StringBuilder();

        int i = 0;
        while (i < data.length && data[i] != 0) {
            ret.append((char) data[i]);
            i++;
        }

        return ret.toString();
    }
}
