package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a physical file on the local disk prepared for network transfer.
 * Implements the Transferable interface to standardize payload handling across the application.
 */
public final class FilePackage implements Transferable {
    private final File file;

    /**
     * Constructs a FilePackage and validates the file's integrity.
     *
     * @param file The java.io.File object representing the file to be sent.
     * @throws NullPointerException if the provided file is null.
     * @throws IllegalArgumentException if the provided path is a directory or cannot be found.
     */
    public FilePackage(File file) {
        this.file = Objects.requireNonNull(file, "File is required");
        if (!file.isFile()) {
            // Using getAbsolutePath() makes debugging significantly easier if a user drags a folder instead of a file
            throw new IllegalArgumentException("The selected path is not a valid file: " + file.getAbsolutePath());
        }
    }

    /**
     * Retrieves the exact name of the file for the receiving peer.
     * @return The file name with its extension.
     */
    @Override
    public String getName() {
        return file.getName();
    }

    /**
     * Retrieves the total size of the file.
     * This is critical for calculating the percentage on the GUI's TransferProgressBar.
     * @return The file size in bytes.
     */
    @Override
    public long getSize() {
        return file.length();
    }

    /**
     * Opens a FileInputStream to read the file sequentially.
     * This ensures the application streams the data in chunks rather than loading it all at once,
     * preventing OutOfMemory errors when transferring massive files.
     *
     * @return An InputStream for reading the file's binary data.
     * @throws IOException If the file cannot be read due to OS permissions or unexpected deletion.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    /**
     * Returns the underlying File object.
     * @return The original java.io.File object.
     */
    public File getFile() {
        return file;
    }
}