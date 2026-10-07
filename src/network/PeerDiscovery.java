package network;

import models.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discovers peers by sending and receiving UDP multicast announcements.
 */
public final class PeerDiscovery {
    private static final String DISCOVERY_MESSAGE = "FILE-SHARE-PEER";
    private static final int PORT = 5555;

    private final List<Peer> peers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public void start() {
        if (running.compareAndSet(false, true)) {
            stopped.set(false);
            discover();
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
            socket.setLoopbackMode(true);
            InetAddress multicastAddress = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(multicastAddress);

            byte[] message = DISCOVERY_MESSAGE.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    message, message.length, multicastAddress, PORT);
            socket.send(packet);

            while (running.get()) {
                byte[] buffer = new byte[256];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                String payload = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
                if (DISCOVERY_MESSAGE.equals(payload)) {
                    continue;
                }
                addPeer(receivePacket.getAddress().getHostAddress(), receivePacket.getAddress().getHostName());
            }
        } catch (IOException | SecurityException exception) {
            if (!stopped.get()) {
                throw new IllegalStateException("Peer discovery failed", exception);
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
