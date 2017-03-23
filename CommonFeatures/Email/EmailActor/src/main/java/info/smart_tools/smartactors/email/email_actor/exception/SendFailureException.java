package info.smart_tools.smartactors.email.email_actor.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for failed email sending, contains list of email addresses which messages were not sent.
 */
public class SendFailureException extends Exception {

    /**
     * list of emails
     */
    private List<String> emails = new ArrayList<>();

    /**
     * Constructor
     */
    public SendFailureException() {
        super("Failed to send messages to ");
    }

    /**
     * Setter for bad email addresses
     * @param email email address which message were not sent
     */
    public void addEmail(final String email) {
        this.emails.add(email);
    }

    /**
     * Getter for list with failed emails
     * @return emails
     */
    public List<String> getEmails() {
        return emails;
    }
}
