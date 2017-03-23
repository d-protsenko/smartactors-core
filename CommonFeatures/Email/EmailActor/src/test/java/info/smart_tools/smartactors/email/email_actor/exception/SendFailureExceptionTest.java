package info.smart_tools.smartactors.email.email_actor.exception;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link SendFailureException}.
 */
public class SendFailureExceptionTest {
    @Test
    public void Should_getConstructedAndStoreAttributes()
            throws Exception {
        SendFailureException exception = new SendFailureException();

        exception.addEmail("some_email");
        assertEquals(exception.getEmails(), Collections.singletonList("some_email"));
    }
}