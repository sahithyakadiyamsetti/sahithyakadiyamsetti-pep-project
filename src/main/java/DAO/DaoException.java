package DAO;

/**
 * DaoException is a custom unchecked exception designed to handle errors
 * that may arise within the DAO (Data Access Object) layer.
 * By extending RuntimeException, this exception is unchecked, meaning it does
 * not need to be explicitly handled or declared in method signatures.
 */
public class DaoException extends RuntimeException {

    /**
     * serialVersionUID is a unique identifier for ensuring compatibility during
     * the serialization and deserialization process. It helps to prevent
     * InvalidClassException by maintaining version consistency between the sender
     * and receiver of a serialized object.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DaoException with the provided message.
     *
     * @param message The detailed message associated with the exception,
     *                which can be retrieved later using the getMessage() method.
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Creates a new DaoException with both a message and a cause.
     * Note that the message associated with the cause is not automatically
     * included in this exception's message.
     *
     * @param message The detailed message for the exception.
     * @param cause   The underlying reason for the exception, which can be retrieved
     *                using the getCause() method. A null value indicates no specific cause.
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
