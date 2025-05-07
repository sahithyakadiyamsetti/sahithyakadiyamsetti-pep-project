package DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Message;
import Util.ConnectionUtil;

/**
 * MessageDao is responsible for interacting with the "message" table in the database,
 * offering methods for performing CRUD (Create, Read, Update, Delete) operations on message data.
 */
public class MessageDao implements BaseDao<Message> {

    // Logger instance for logging messages related to database operations
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDao.class);

    /**
     * A utility method for logging SQL exceptions and throwing a custom DaoException.
     *
     * @param e The SQLException that was thrown during the database operation.
     * @param sql The SQL query that caused the exception.
     * @param errorMessage A custom message that describes the error.
     */
    private void handleSQLException(SQLException e, String sql, String errorMessage) {
        LOGGER.error("SQLException occurred: {}", e.getMessage());
        LOGGER.error("SQL State: {}", e.getSQLState());
        LOGGER.error("Error Code: {}", e.getErrorCode());
        LOGGER.error("SQL Query: {}", sql);
        throw new DaoException(errorMessage, e);
    }

    /**
     * Fetches a message from the database by its unique identifier.
     *
     * @param id The identifier of the message to retrieve.
     * @return An Optional containing the Message if found, otherwise an empty Optional.
     */
    @Override
    public Optional<Message> getById(int id) {
        String sql = "SELECT * FROM message WHERE message_id = ?";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error retrieving message with ID: " + id);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all messages stored in the database.
     *
     * @return A List of all messages present in the database.
     */
    @Override
    public List<Message> getAll() {
        String sql = "SELECT * FROM message";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error retrieving all messages.");
        }
        return messages;
    }

    /**
     * Retrieves messages posted by a specific account.
     *
     * @param accountId The ID of the account whose messages are to be fetched.
     * @return A List of messages from the specified account.
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                messages = mapResultSetToList(rs);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error retrieving messages by account ID: " + accountId);
        }
        return messages;
    }

    /**
     * Inserts a new message into the database.
     *
     * @param message The Message object to be inserted.
     * @return The inserted message, with the auto-generated ID included.
     */
    @Override
    public Message insert(Message message) {
        String sql = "INSERT INTO message(posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    return new Message(generatedId, message.getPosted_by(), message.getMessage_text(),
                            message.getTime_posted_epoch());
                } else {
                    throw new DaoException("Failed to insert message, ID not generated.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error inserting message.");
        }
        throw new DaoException("Failed to insert message.");
    }

    /**
     * Updates an existing message in the database.
     *
     * @param message The Message object with updated values.
     * @return true if the update was successful, false otherwise.
     */
    @Override
    public boolean update(Message message) {
        String sql = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        int rowsUpdated = 0;
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.setInt(4, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error updating message with ID: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Deletes a specific message from the database.
     *
     * @param message The Message object to be deleted.
     * @return true if the deletion was successful, false otherwise.
     */
    @Override
    public boolean delete(Message message) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        int rowsUpdated = 0;
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error deleting message with ID: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Maps a ResultSet row to a Message object.
     *
     * @param rs The ResultSet row containing message data.
     * @return The corresponding Message object.
     * @throws SQLException If an error occurs while accessing the ResultSet.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        int messageId = rs.getInt("message_id");
        int postedBy = rs.getInt("posted_by");
        String messageText = rs.getString("message_text");
        long timePostedEpoch = rs.getLong("time_posted_epoch");
        return new Message(messageId, postedBy, messageText, timePostedEpoch);
    }

    /**
     * Converts a ResultSet into a List of Message objects.
     *
     * @param rs The ResultSet to be converted.
     * @return A List of Message objects.
     * @throws SQLException If an error occurs while processing the ResultSet.
     */
    private List<Message> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }
}
