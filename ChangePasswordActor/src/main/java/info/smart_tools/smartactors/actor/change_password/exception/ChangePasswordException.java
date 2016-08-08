package info.smart_tools.smartactors.actor.change_password.exception;

/**
 * Exception for errors from {@link info.smart_tools.smartactors.actor.change_password.ChangePasswordActor}
 */
public class ChangePasswordException extends Exception {

    /**
      * Constructor with specific error message as argument
      * @param message specific error message
    */
    public ChangePasswordException(final String message) {
        super(message);
    }

    /**
      * Constructor with specific error message and specific cause as arguments
      * @param message specific error message
      * @param cause specific cause
    */
    public ChangePasswordException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
