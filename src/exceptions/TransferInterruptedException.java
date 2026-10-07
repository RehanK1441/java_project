package exceptions;

/**
 * Thrown when an active file transfer is halted prematurely.
 * This can be triggered manually by the user or automatically due to network failure.
 */
public final class TransferInterruptedException extends Exception {
    
    /**
     * Constructs the exception with a specific reason for the interruption.
     * @param message The reason the transfer was stopped.
     */
    public TransferInterruptedException(String message) {
        super(message);
    }

    /**
     * Constructs the exception by wrapping an underlying network or I/O error.
     * @param message The context of what failed (e.g., "Connection lost during transfer").
     * @param cause The underlying exception (e.g., SocketException) that triggered this failure.
     */
    public TransferInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}