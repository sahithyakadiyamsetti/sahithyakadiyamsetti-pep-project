package Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DAO.MessageDao;
import DAO.DaoException;
import Model.Account;
import Model.Message;
import io.javalin.http.NotFoundResponse;

/**
 * Service class for handling business logic related to messages.
 * It interacts with the DAO layer to perform CRUD operations and validates permissions.
 */
public class MessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
    private static final String DB_ACCESS_ERROR_MSG = "Database access error occurred";
    
    private MessageDao messageDao;

    // Default constructor initializing the MessageDao object
    public MessageService() {
        this.messageDao = new MessageDao();
    }

    // Constructor for using a custom MessageDao (useful for testing)
    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    /**
     * Retrieves a message by its ID.
     * 
     * @param id The ID of the message to fetch.
     * @return An Optional containing the found message.
     * @throws ServiceException If the message isn't found or if there's a DAO issue.
     */
    public Optional<Message> getMessageById(int id) {
        LOGGER.info("Fetching message with ID: {}", id);
        try {
            Optional<Message> message = messageDao.getById(id);
            if (message.isEmpty()) {
                throw new ServiceException("Message not found");
            }
            LOGGER.info("Fetched message: {}", message.get());
            return message;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Retrieves all messages in the system.
     * 
     * @return A list of all messages.
     * @throws ServiceException If there's an issue accessing the database.
     */
    public List<Message> getAllMessages() {
        LOGGER.info("Fetching all messages");
        try {
            List<Message> messages = messageDao.getAll();
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Retrieves messages posted by a specific account.
     * 
     * @param accountId The account ID for which messages are retrieved.
     * @return A list of messages posted by the specified account.
     * @throws ServiceException If there's an issue with the DAO layer.
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        LOGGER.info("Fetching messages for account ID: {}", accountId);
        try {
            List<Message> messages = messageDao.getMessagesByAccountId(accountId);
            LOGGER.info("Fetched {} messages", messages.size());
            return messages;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Creates a new message and checks for account permissions.
     * 
     * @param message The message to create.
     * @param account The account performing the action.
     * @return The created message.
     * @throws ServiceException If the message is invalid or the account lacks permissions.
     */
    public Message createMessage(Message message, Optional<Account> account) {
        LOGGER.info("Creating message: {}", message);
        if (account.isEmpty()) {
            throw new ServiceException("Account must exist to post a message");
        }

        validateMessage(message);
        checkAccountPermission(account.get(), message.getPosted_by());

        try {
            Message createdMessage = messageDao.insert(message);
            LOGGER.info("Created message: {}", createdMessage);
            return createdMessage;
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Updates an existing message if the account has proper permissions.
     * 
     * @param message The message to update.
     * @return The updated message.
     * @throws ServiceException If the message isn't found or the account lacks permissions.
     */
    public Message updateMessage(Message message) {
        LOGGER.info("Updating message: {}", message.getMessage_id());

        Optional<Message> existingMessage = getMessageById(message.getMessage_id());
        if (existingMessage.isEmpty()) {
            throw new ServiceException("Message not found");
        }

        existingMessage.get().setMessage_text(message.getMessage_text());
        validateMessage(existingMessage.get());

        try {
            messageDao.update(existingMessage.get());
            LOGGER.info("Updated message: {}", existingMessage.get());
            return existingMessage.get();
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Deletes a message after ensuring the account has proper permissions.
     * 
     * @param message The message to delete.
     * @throws ServiceException If the message is not found or if there is a database error.
     */
    public void deleteMessage(Message message) {
        LOGGER.info("Deleting message: {}", message);

        try {
            boolean deleted = messageDao.delete(message);
            if (deleted) {
                LOGGER.info("Message deleted: {}", message);
            } else {
                throw new NotFoundResponse("Message not found");
            }
        } catch (DaoException e) {
            throw new ServiceException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    /**
     * Validates the content of a message, ensuring it isn't null, empty, or too long.
     * 
     * @param message The message to validate.
     * @throws ServiceException If the message is invalid.
     */
    private void validateMessage(Message message) {
        LOGGER.info("Validating message: {}", message);
        String messageText = message.getMessage_text().trim();

        if (messageText.isEmpty()) {
            throw new ServiceException("Message text cannot be empty");
        }

        if (messageText.length() > 254) {
            throw new ServiceException("Message text cannot exceed 254 characters");
        }
    }

    /**
     * Checks if the account has permission to modify the message.
     * 
     * @param account The account attempting to perform the action.
     * @param postedBy The account ID that originally posted the message.
     * @throws ServiceException If the account is unauthorized to modify the message.
     */
    private void checkAccountPermission(Account account, int postedBy) {
        LOGGER.info("Checking account permissions for message modification");
        if (account.getAccount_id() != postedBy) {
            throw new ServiceException("Unauthorized account to modify this message");
        }
    }
}
