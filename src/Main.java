import gui.Dashboard;
import network.PeerDiscovery;

import javax.swing.SwingUtilities;

/**
 * Application entry point. It owns application-level orchestration while
 * individual components remain responsible for their own behavior.
 */
public final class Main {
    
    private Main() {
        // Prevents instantiation of the entry point class
    }

    public static void main(String[] args) {
        // Dynamically grabs the OS username (e.g., Rehan) for network identification
        String localHostname = System.getProperty("user.name", "Unknown-Node");
        
        PeerDiscovery peerDiscovery = new PeerDiscovery(localHostname);

        // This method now inherently spawns its own daemon thread, preventing UI blocking
        peerDiscovery.start();

        // Launches the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new Dashboard(peerDiscovery));
    }
}