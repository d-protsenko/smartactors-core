package info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;

/**
 * Exception for error in {@link IResolutionStrategy} methods
 */
public class ResolutionStrategyException extends Exception {

    /**
     * Constructor with specific error message as argument
     * @param message specific error message
     */
    public ResolutionStrategyException(final String message) {
        super(message);
    }

    /**
     * Constructor with specific error message and specific cause as arguments
     * @param message specific error message
     * @param cause specific cause
     */

    public ResolutionStrategyException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with specific cause as argument
     * @param cause specific cause
     */
    public ResolutionStrategyException(final Throwable cause) {
        super(cause);
    }
}
