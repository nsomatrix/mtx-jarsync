package nhquyet.zip;

/**
 * Signals that a data format error has occurred during decompression.
 */
public class DataFormatException extends Exception {
    public DataFormatException() {
        super();
    }

    public DataFormatException(String s) {
        super(s);
    }
}
