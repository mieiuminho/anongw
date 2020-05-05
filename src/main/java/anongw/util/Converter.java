package anongw.util;

public final class Converter {

    private Converter() {
    }

    /**
     * A utility method to convert the byte array data into a string representation.
     *
     * @param a the array of bytes
     * @return null if the array is null
     */
    public static StringBuilder fromBytes(final byte[] a) {
        if (a == null)
            return null;

        StringBuilder ret = new StringBuilder();

        int i = 0;
        while (i < a.length && a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }

        return ret;
    }
}
