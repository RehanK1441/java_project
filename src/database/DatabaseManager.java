package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates and owns the SQLite database connection used by transfer history.
 */
public final class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:file_transfer.db";

    private final String databaseUrl;

    public DatabaseManager() {
        this(DATABASE_URL);
    }

    DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    public void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS transfer_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "peer_ip TEXT NOT NULL, "
                    + "file_name TEXT NOT NULL, "
                    + "file_size INTEGER NOT NULL, "
                    + "status TEXT NOT NULL, "
                    + "transferred_at TIMESTAMP NOT NULL)");
        }
    }
}
