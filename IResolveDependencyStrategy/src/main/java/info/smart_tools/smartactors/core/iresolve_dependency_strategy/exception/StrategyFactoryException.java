package info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception;

/**
 * Exception for error in {@link info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory} methods
 */
public class StrategyFactoryException extends Exception {

    /**
     * Default constructor
     */
    private StrategyFactoryException() {
        super();
    }

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public StrategyFactoryException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public StrategyFactoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public StrategyFactoryException(final Throwable cause) {
        super(cause);
    }
}
