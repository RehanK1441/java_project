package network;

import models.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discovers peers by broadcasting and receiving UDP multicast announcements.
 * Operates on a dedicated background thread to prevent GUI freezing.
 */
public final class PeerDiscovery {
    // Appended a colon to allow dynamic hostname injection
    private static final String DISCOVERY_PREFIX = "FILE-SHARE-PEER:";
    private static final int PORT = 5555;

    private final List<Peer> peers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final String localHostname;

    /**
     * Constructs the discovery engine.
     * @param localHostname The name of this device to broadcast to others.
     */
    public PeerDiscovery(String localHostname) {
        this.localHostname = localHostname;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            stopped.set(false);
            // CRITICAL: Spawns a dedicated thread so the GUI remains completely responsive
            new Thread(this::discover, "UDP-Discovery-Thread").start();
        }
    }

    public void stop() {
        running.set(false);
        stopped.set(true);
    }

    public boolean isRunning() {
        return running.get();
    }

    public List<Peer> getPeers() {
        return Collections.unmodifiableList(new ArrayList<>(peers));
    }

    private void discover() {
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress("0.0.0.0", PORT));
            socket.setTimeToLive(1);
            
            // In Java, false MEANS loopback is enabled. Crucial for testing on a single machine.
            socket.setLoopbackMode(false); 
            
            // Set a timeout so socket.receive() checks the 'running' flag every 1 second
            socket.setSoTimeout(1000);

            InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(multicastAddress);

            // Construct payload with hostname to avoid slow reverse DNS lookups on the receiver end
            String broadcastMessage = DISCOVERY_PREFIX + localHostname;
            byte[] message = broadcastMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(message, message.length, multicastAddress, PORT);
            
            // Announce presence to the network
            socket.send(packet);

            while (running.get()) {
                try {
                    byte[] buffer = new byte[256];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivePacket);
                    
                    String payload = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                    
                    if (payload.startsWith(DISCOVERY_PREFIX)) {
                        String peerName = payload.substring(DISCOVERY_PREFIX.length());
                        String peerIp = receivePacket.getAddress().getHostAddress();
                        
                        // Ignore our own broadcast
                        if (!peerName.equals(localHostname)) {
                            addPeer(peerIp, peerName);
                        }
                    }
                } catch (SocketTimeoutException timeout) {
                    // Expected behavior. Allows the while-loop to check running.get() and exit cleanly if stopped.
                }
            }
            socket.leaveGroup(multicastAddress);
            
        } catch (IOException | SecurityException exception) {
            if (!stopped.get()) {
                // Fails gracefully and alerts developers to local firewall blocks
                System.err.println("Peer discovery failed: " + exception.getMessage());
            }
        } finally {
            running.set(false);
        }
    }

    private void addPeer(String ipAddress, String hostname) {
        peers.removeIf(peer -> peer.getIpAddress().equals(ipAddress));
        peers.add(new Peer(ipAddress, hostname));
    }
}