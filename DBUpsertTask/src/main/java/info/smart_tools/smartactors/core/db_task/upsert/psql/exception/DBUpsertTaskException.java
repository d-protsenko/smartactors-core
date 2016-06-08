package info.smart_tools.smartactors.core.db_task.upsert.psql.exception;

/**
 * Custom exception for DBUpsertTask
 */
public class DBUpsertTaskException extends Exception {

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public DBUpsertTaskException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
