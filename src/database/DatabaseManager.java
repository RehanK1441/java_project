package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database lifecycle for the LAN-Drop application.
 * Handles the initial connection and the creation of the database schema.
 */
public final class DatabaseManager {
    // Uses a local SQLite file in the project root directory
    private static final String DATABASE_URL = "jdbc:sqlite:file_transfer.db";

    private final String databaseUrl;

    /**
     * Default constructor using the standard database URL.
     */
    public DatabaseManager() {
        this(DATABASE_URL);
    }

    /**
     * Overloaded constructor for testing purposes (e.g., passing an in-memory database URL).
     * @param databaseUrl The JDBC URL to connect to.
     */
    DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    /**
     * Establishes a connection to the SQLite database.
     * @return A valid JDBC Connection object.
     * @throws SQLException If the database access fails.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    /**
     * Initializes the database schema.
     * Creates the transfer_history table if it does not already exist.
     * @throws SQLException If the SQL execution fails.
     */
    public void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            
            // Note: SQLite uses TEXT for timestamps since it lacks a native DATETIME type
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS transfer_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "peer_ip TEXT NOT NULL, "
                    + "file_name TEXT NOT NULL, "
                    + "file_size INTEGER NOT NULL, "
                    + "status TEXT NOT NULL, "
                    + "transferred_at TEXT NOT NULL)");
        }
    }
}