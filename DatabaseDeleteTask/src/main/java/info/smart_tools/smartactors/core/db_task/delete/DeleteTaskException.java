package info.smart_tools.smartactors.core.db_task.delete;

public class DeleteTaskException extends Exception {
    public DeleteTaskException(String message) {
        super(message);
    }

    public DeleteTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}