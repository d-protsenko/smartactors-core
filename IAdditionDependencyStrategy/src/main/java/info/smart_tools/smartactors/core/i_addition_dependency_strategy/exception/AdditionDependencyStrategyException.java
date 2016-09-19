package info.smart_tools.smartactors.core.i_addition_dependency_strategy.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy} methods
 */
public class AdditionDependencyStrategyException extends Exception {

    /**
     * Constructor with specific error message as argument
     *
     * @param message specific error message
     */
    public AdditionDependencyStrategyException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     *
     * @param message specific error message
     * @param cause   specific cause
     */

    public AdditionDependencyStrategyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     *
     * @param cause specific cause
     */
    public AdditionDependencyStrategyException(final Throwable cause) {
        super(cause);
    }
}
