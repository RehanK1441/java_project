package exceptions;

/**
 * Thrown when a requested transfer target cannot be located on the local network.
 * This typically occurs if a peer disconnects unexpectedly before a transfer begins.
 */
public final class PeerNotFoundException extends Exception {
    
    /**
     * Constructs the exception with the specific IP address that failed to resolve.
     * @param ipAddress The IP address of the missing peer.
     */
    public PeerNotFoundException(String ipAddress) {
        super("Peer not found on the local network: " + ipAddress);
    }
}