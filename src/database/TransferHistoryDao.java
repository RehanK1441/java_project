package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for executing queries against the transfer history database.
 * Isolates all SQL logic from the rest of the application.
 */
public final class TransferHistoryDao {
    private final DatabaseManager databaseManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs the DAO with an injected DatabaseManager.
     * @param databaseManager The manager to use for obtaining connections.
     */
    public TransferHistoryDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Logs a new file transfer into the database using a PreparedStatement to prevent SQL injection.
     *
     * @param peerIp   The IP address of the sender/receiver.
     * @param fileName The name of the file transferred.
     * @param fileSize The size of the file in bytes.
     * @param status   The outcome (e.g., "SENT", "RECEIVED", "FAILED").
     * @throws SQLException If the insert operation fails.
     */
    public void insert(String peerIp, String fileName, long fileSize, String status) throws SQLException {
        String sql = "INSERT INTO transfer_history (peer_ip, file_name, file_size, status, transferred_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, peerIp);
            statement.setString(2, fileName);
            statement.setLong(3, fileSize);
            statement.setString(4, status);
            // Convert LocalDateTime to String to ensure SQLite driver compatibility
            statement.setString(5, LocalDateTime.now().format(DATE_FORMATTER));
            
            statement.executeUpdate();
        }
    }

    /**
     * Retrieves the complete history of all file transfers.
     *
     * @return A formatted List of Strings representing the transfer logs, ordered by most recent first.
     * @throws SQLException If the query operation fails.
     */
    public List<String> selectAll() throws SQLException {
        List<String> history = new ArrayList<>();
        String sql = "SELECT peer_ip, file_name, file_size, status, transferred_at "
                + "FROM transfer_history ORDER BY transferred_at DESC";
        
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                history.add(String.format("[%s] %s | %s | %d bytes | %s",
                        resultSet.getString("transferred_at"),
                        resultSet.getString("status"),
                        resultSet.getString("file_name"),
                        resultSet.getLong("file_size"),
                        resultSet.getString("peer_ip")));
            }
        }
        return history;
    }
}