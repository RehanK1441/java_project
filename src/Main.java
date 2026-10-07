import gui.Dashboard;
import network.PeerDiscovery;

import javax.swing.SwingUtilities;

/**
 * Application entry point. It owns application-level orchestration while
 * individual components remain responsible for their own behavior.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        PeerDiscovery peerDiscovery = new PeerDiscovery();

        SwingUtilities.invokeLater(() -> new Dashboard(peerDiscovery));

        Thread discoveryThread = new Thread(peerDiscovery::start, "peer-discovery");
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }
}
