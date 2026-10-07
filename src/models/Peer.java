package models;

import java.util.Objects;

/**
 * Represents a remote device discovered on the local network.
 * This class acts as a data model for managing active connections.
 */
public final class Peer {
    private final String ipAddress;
    private final String hostname;
    private boolean active;

    /**
     * Constructs a new Peer instance.
     *
     * @param ipAddress The IPv4 address of the discovered peer.
     * @param hostname  The device name or custom username of the peer.
     * @throws NullPointerException if ipAddress or hostname is null.
     */
    public Peer(String ipAddress, String hostname) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP address is required");
        this.hostname = Objects.requireNonNull(hostname, "Hostname is required");
        this.active = true;
    }

    /**
     * Retrieves the network IP address of the peer.
     * @return The IP address as a String.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Retrieves the network hostname or username of the peer.
     * @return The hostname as a String.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Checks if the peer is currently active on the network.
     * @return true if active, false otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Updates the active status of the peer.
     * @param active the new status to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Ensures equality is strictly evaluated based on the unique IP address.
     * Critical for accurate tracking in Collections like ConcurrentHashMap.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return ipAddress.equals(peer.ipAddress);
    }

    /**
     * Generates a hash code based on the IP address.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ipAddress);
    }

    /**
     * Formats the peer information for GUI display (e.g., in a JList).
     */
    @Override
    public String toString() {
        return hostname + " (" + ipAddress + ")";
    }
}