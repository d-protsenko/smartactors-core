package info.smart_tools.smartactors.core.idatabase.exception;

/**
 * Exception class for {@link info.smart_tools.smartactors.core.idatabase.IDataBase}
 */
public class IDataBaseException extends Exception {

    /**
     * Constructor with specific error message
     * @param message specific error message
     */
    public IDataBaseException(final String message) {
        super(message);
    }


    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public IDataBaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as arguments
     * @param cause specific cause
     */
    public IDataBaseException(final Throwable cause) {
        super(cause);
    }
}
