package info.smart_tools.smartactors.core.proof_of_assumption.old_generator;

/**
 * Checked exception thrown when a {@code ObjectWrapperGenerator} can not
 * generate wrapper class for some reason, typically
 * due to a wrong symbols in class name or its methods names.
 *
 */
public class GeneratorException extends Exception {
    public GeneratorException() {
        super();
    }

    public GeneratorException(String message) {
        super(message);
    }

    public GeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneratorException(Throwable cause) {
        super(cause);
    }
}
