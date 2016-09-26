package info.smart_tools.smartactors.core.ifeature_loader.exceptions;

/**
 * Exception thrown by {@link info.smart_tools.smartactors.core.ifeature_loader.IFeatureLoader} when error occurs loading a feature.
 */
public class FeatureLoadException extends Exception {
    /**
     * The constructor.
     *
     * @param msg    the message
     */
    public FeatureLoadException(final String msg) {
        super(msg);
    }

    /**
     * The constructor.
     *
     * @param msg    the message
     * @param cause  the cause
     */
    public FeatureLoadException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
