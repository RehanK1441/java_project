package network;

import exceptions.TransferInterruptedException;
import models.FilePackage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Listens for incoming TCP connections and writes the received file payload to the local disk.
 * Designed to be executed in a dedicated background thread to ensure the application remains responsive.
 */
public final class FileReceiver {
    private final File destinationDirectory;
    private final int port;

    /**
     * Constructs a FileReceiver.
     * 
     * @param destinationDirectory The directory where received files will be saved.
     * @param port The TCP port to listen on for incoming transfers.
     */
    public FileReceiver(File destinationDirectory, int port) {
        this.destinationDirectory = destinationDirectory;
        this.port = port;
    }

    /**
     * Blocks and waits for a peer to connect, then decodes the metadata and downloads the file.
     * 
     * @return A FilePackage representing the successfully downloaded file.
     * @throws TransferInterruptedException If the network drops or the stream is corrupted.
     */
    public FilePackage receive() throws TransferInterruptedException {
        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept();
             InputStream input = new BufferedInputStream(socket.getInputStream());
             DataInputStream dataInput = new DataInputStream(input)) {
             
            // 1. Decode Metadata Protocol
            int nameLength = dataInput.readInt();
            byte[] nameBytes = new byte[nameLength];
            dataInput.readFully(nameBytes);
            String fileName = new String(nameBytes, StandardCharsets.UTF_8);
            long size = dataInput.readLong();

            // 2. Prepare Destination File
            File destination = new File(destinationDirectory, fileName);
            Files.createDirectories(destination.getParentFile().toPath());
            
            // 3. Stream Payload to Disk
            try (var output = Files.newOutputStream(destination.toPath())) {
                byte[] buffer = new byte[8192]; // 8KB buffer for optimal local I/O
                long remaining = size;
                
                while (remaining > 0) {
                    int bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (bytesRead < 0) {
                        throw new TransferInterruptedException("The transfer ended abruptly before the declared file size was received.");
                    }
                    output.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }
            return new FilePackage(destination);
            
        } catch (IOException e) {
            throw new TransferInterruptedException("Failed to receive file on port " + port + ": " + e.getMessage(), e);
        }
    }
}