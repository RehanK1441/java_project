package models;

import java.io.IOException;
import java.io.InputStream;

/**
 * Common polymorphic contract for objects that can be transferred across the network.
 * Supports different data payloads (e.g., standard files, compressed directories).
 */
public interface Transferable {
    
    /**
     * Retrieves the exact name of the file being transferred.
     * @return The file name with its extension.
     */
    String getName();

    /**
     * Retrieves the total byte size of the file for progress tracking.
     * @return The file size in bytes.
     */
    long getSize();

    /**
     * Opens an input stream to read the file's binary data sequentially.
     * @return An InputStream for reading the file contents.
     * @throws IOException If the file cannot be accessed or read.
     */
    InputStream getInputStream() throws IOException;
}