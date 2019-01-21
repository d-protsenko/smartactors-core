package info.smart_tools.smartactors.base.interfaces.i_registration_strategy.exception;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Exception for error in {@link IResolveDependencyStrategy} methods
 */
public class RegistrationStrategyException extends Exception {

    /**
     * Constructor with specific error message as argument
     *
     * @param message specific error message
     */
    public RegistrationStrategyException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     *
     * @param message specific error message
     * @param cause   specific cause
     */

    public RegistrationStrategyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     *
     * @param cause specific cause
     */
    public RegistrationStrategyException(final Throwable cause) {
        super(cause);
    }
}
