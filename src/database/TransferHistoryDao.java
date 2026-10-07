package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC data access for transfer history records.
 */
public final class TransferHistoryDao {
    private final DatabaseManager databaseManager;

    public TransferHistoryDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void insert(String peerIp, String fileName, long fileSize, String status) throws SQLException {
        String sql = "INSERT INTO transfer_history (peer_ip, file_name, file_size, status, transferred_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, peerIp);
            statement.setString(2, fileName);
            statement.setLong(3, fileSize);
            statement.setString(4, status);
            statement.setObject(5, LocalDateTime.now());
            statement.executeUpdate();
        }
    }

    public List<String> selectAll() throws SQLException {
        List<String> history = new ArrayList<>();
        String sql = "SELECT peer_ip, file_name, file_size, status, transferred_at "
                + "FROM transfer_history ORDER BY transferred_at DESC";
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                history.add(resultSet.getString("peer_ip") + " | "
                        + resultSet.getString("file_name") + " | "
                        + resultSet.getLong("file_size") + " bytes | "
                        + resultSet.getString("status") + " | "
                        + resultSet.getObject("transferred_at"));
            }
        }
        return history;
    }
}
