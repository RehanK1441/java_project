package models;

import java.io.IOException;
import java.io.InputStream;

/**
 * Common contract for objects that can be transferred between peers.
 */
public interface Transferable {
    String getName();

    long getSize();

    InputStream getInputStream() throws IOException;
}
