package info.smart_tools.smartactors.core.db_task.search_by_id.exception;

/**
 * Custom exception for DBGetByIdTask
 */
public class DBSearchByIdTaskException extends Exception {

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public DBSearchByIdTaskException(final String message, final Throwable cause) {
        super(message, cause);
    }
}