package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A transferable file with metadata and a stream for its contents.
 */
public final class FilePackage implements Transferable {
    private final File file;

    public FilePackage(File file) {
        this.file = Objects.requireNonNull(file, "File is required");
        if (!file.isFile()) {
            throw new IllegalArgumentException("The selected path is not a file: " + file);
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public File getFile() {
        return file;
    }
}
