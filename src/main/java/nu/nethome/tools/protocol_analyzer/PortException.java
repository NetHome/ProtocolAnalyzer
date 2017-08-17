package nu.nethome.tools.protocol_analyzer;

/**
 *
 */
public class PortException extends Exception {
    public PortException(String message) {
        super(message);
    }

    public PortException(String message, Throwable cause) {
        super(message, cause);
    }
}
