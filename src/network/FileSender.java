package network;

import models.FilePackage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Sends a file package over a TCP connection.
 */
public final class FileSender {
    public void send(FilePackage filePackage, String ipAddress, int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, port), 5000);
            try (OutputStream output = socket.getOutputStream();
                 DataOutputStream dataOutput = new DataOutputStream(output);
                 InputStream fileInput = filePackage.getInputStream()) {
                byte[] nameBytes = filePackage.getName().getBytes(StandardCharsets.UTF_8);
                dataOutput.writeInt(nameBytes.length);
                dataOutput.write(nameBytes);
                dataOutput.writeLong(filePackage.getSize());
                fileInput.transferTo(output);
            }
        }
    }
}
