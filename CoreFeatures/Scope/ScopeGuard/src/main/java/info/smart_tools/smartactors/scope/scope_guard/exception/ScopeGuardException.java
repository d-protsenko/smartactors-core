package info.smart_tools.smartactors.scope.scope_guard.exception;

import info.smart_tools.smartactors.scope.scope_guard.IScopeGuard;

/**
 * Exception for error in {@link IScopeGuard} methods
 */
public class ScopeGuardException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public ScopeGuardException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public ScopeGuardException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public ScopeGuardException(final Throwable cause) {
        super(cause);
    }
}
