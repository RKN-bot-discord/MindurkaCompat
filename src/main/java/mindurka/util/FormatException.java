package mindurka.util;

public class FormatException extends RuntimeException {
    public FormatException(String message) { super(message); }
    public FormatException(String message, Throwable source) { super(message, source); }
}
