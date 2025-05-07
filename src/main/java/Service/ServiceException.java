package Service;

/**
 * ServiceException is a custom unchecked exception used in the service layer
 * to provide more detailed error handling and specific message propagation.
 * This exception wraps other exceptions that may occur in the service layer.
 *
 * Since ServiceException extends RuntimeException, it is unchecked and does
 * not need to be explicitly caught or declared. Unlike DaoException, this
 * exception does not alter serialization behavior, so a serialVersionUID is
 * not necessary.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructor to create a ServiceException with a custom error message.
     * 
     * @param message The message that describes the error.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructor to create a ServiceException that wraps an existing cause.
     * 
     * @param cause The original exception that caused this ServiceException.
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor to create a ServiceException with both a custom error message
     * and the original cause of the exception.
     * 
     * @param message The error message that describes the issue.
     * @param cause   The exception that triggered this ServiceException.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
