package info.smart_tools.smartactors.actors.mailing.email;

import org.testng.annotations.Test;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class SMTPMessageAdaptorTest {
    private void verifyMessageStream(InputStream is, String expected)
            throws Exception {
        byte[] bytes = new byte[expected.length() + 1];
        assertEquals(is.read(bytes), expected.length());
        assertEquals(new String(bytes).substring(0, expected.length()), expected);
    }

    @Test
    public void Should_CallMimeMessagesWriteToMethod_When_InputStreamRequested()
            throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        SMTPMessageAdaptor messageAdaptor = new SMTPMessageAdaptor(mimeMessage);

        doAnswer(invocationOnMock -> {
            OutputStream os = (OutputStream)invocationOnMock.getArguments()[0];
            os.write("This is an MIME message.".getBytes());
            return null;
        }).when(mimeMessage).writeTo(any());

        verifyMessageStream(messageAdaptor.get7bit(), "This is an MIME message.");
        verifyMessageStream(messageAdaptor.get8Bit(), "This is an MIME message.");
    }
}
