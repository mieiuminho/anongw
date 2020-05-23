package anongw.util;

public final class Converter {

    private Converter() {}

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
