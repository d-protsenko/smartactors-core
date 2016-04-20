package info.smart_tools.smartactors.core.strategy_provider.exception;

/**
 * Exception for runtime error in {@link info.smart_tools.smartactors.core.strategy_provider.IStrategyProviderContainer} methods
 */
public class StrategyProviderException extends Exception {

    /**
     * Default constructor
     */
    private StrategyProviderException() {
        super();
    }

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public StrategyProviderException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */
    public StrategyProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public StrategyProviderException(final Throwable cause) {
        super(cause);
    }


}
