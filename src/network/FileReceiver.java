package network;

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
 * Accepts a TCP file transfer and writes its contents to disk.
 */
public final class FileReceiver {
    private final File destinationDirectory;

    public FileReceiver(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public FilePackage receive() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(5556);
             Socket socket = serverSocket.accept();
             InputStream input = new BufferedInputStream(socket.getInputStream());
             DataInputStream dataInput = new DataInputStream(input)) {
            int nameLength = dataInput.readInt();
            byte[] nameBytes = new byte[nameLength];
            dataInput.readFully(nameBytes);
            String fileName = new String(nameBytes, StandardCharsets.UTF_8);
            long size = dataInput.readLong();

            File destination = new File(destinationDirectory, fileName);
            Files.createDirectories(destination.getParentFile().toPath());
            try (var output = Files.newOutputStream(destination.toPath())) {
                byte[] buffer = new byte[8192];
                long remaining = size;
                while (remaining > 0) {
                    int bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (bytesRead < 0) {
                        throw new IOException("The transfer ended before the declared file size was received");
                    }
                    output.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }
            return new FilePackage(destination);
        }
    }
}
