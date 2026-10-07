package exceptions;

/**
 * Thrown when a transfer is stopped by the user or application.
 */
public final class TransferInterruptedException extends Exception {
    public TransferInterruptedException(String message) {
        super(message);
    }
}
