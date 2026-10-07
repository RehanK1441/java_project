package exceptions;

/**
 * Thrown when a requested peer cannot be found in the peer registry.
 */
public final class PeerNotFoundException extends Exception {
    public PeerNotFoundException(String ipAddress) {
        super("Peer not found: " + ipAddress);
    }
}
