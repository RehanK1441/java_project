package models;

import java.util.Objects;

/**
 * Represents a peer discovered on the local network.
 */
public final class Peer {
    private final String ipAddress;
    private final String hostname;
    private boolean active;

    public Peer(String ipAddress, String hostname) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP address is required");
        this.hostname = Objects.requireNonNull(hostname, "Hostname is required");
        this.active = true;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return hostname + " (" + ipAddress + ")";
    }
}
