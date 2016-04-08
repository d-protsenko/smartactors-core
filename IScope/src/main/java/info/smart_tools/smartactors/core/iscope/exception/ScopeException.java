package info.smart_tools.smartactors.core.iscope.exception;

/**
 * Exception for runtime error in {@link info.smart_tools.scope_interface.IScope} methods
 */
public class ScopeException extends RuntimeException {

    /**
     * Default constructor
     */
    private ScopeException() {

    }

    /**
     * Constructor with 2 args
     * @param message human readable message
     * @param cause cause of exception
     */
    public ScopeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
