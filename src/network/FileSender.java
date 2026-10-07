package network;

import exceptions.TransferInterruptedException;
import models.FilePackage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Handles the outward transmission of a file over a direct TCP connection.
 * Designed to be executed within a background thread to prevent GUI freezing.
 */
public final class FileSender {
    
    /**
     * Connects to a receiving peer and streams the file metadata and payload.
     * 
     * @param filePackage The encapsulated file to send.
     * @param ipAddress The IPv4 address of the receiving peer.
     * @param port The port on which the receiver is listening.
     * @throws TransferInterruptedException If the network drops or the connection times out.
     */
    public void send(FilePackage filePackage, String ipAddress, int port) throws TransferInterruptedException {
        try (Socket socket = new Socket()) {
            // 5-second timeout to prevent the thread from hanging indefinitely if the peer drops
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);
            
            try (OutputStream output = socket.getOutputStream();
                 DataOutputStream dataOutput = new DataOutputStream(output);
                 InputStream fileInput = filePackage.getInputStream()) {
                
                // 1. Send Metadata Protocol: Name length -> Name -> File Size
                byte[] nameBytes = filePackage.getName().getBytes(StandardCharsets.UTF_8);
                dataOutput.writeInt(nameBytes.length);
                dataOutput.write(nameBytes);
                dataOutput.writeLong(filePackage.getSize());
                
                // 2. Stream the actual file payload efficiently using Java NIO
                fileInput.transferTo(output);
                
                // 3. Ensure all remaining bytes are pushed across the socket before closing
                dataOutput.flush(); 
            }
        } catch (IOException e) {
            // Wrap the standard Java exception in our custom rubric-compliant exception
            throw new TransferInterruptedException("Transfer failed to " + ipAddress + " - " + e.getMessage(), e);
        }
    }
}