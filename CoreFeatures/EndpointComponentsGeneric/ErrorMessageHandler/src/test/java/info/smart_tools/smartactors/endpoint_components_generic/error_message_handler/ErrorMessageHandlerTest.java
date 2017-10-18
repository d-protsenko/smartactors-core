package info.smart_tools.smartactors.endpoint_components_generic.error_message_handler;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link ErrorMessageHandler}.
 */
public class ErrorMessageHandlerTest {
    @Test public void Should_throwExceptionWithConfiguredMessage() throws Exception {
        try {
            new ErrorMessageHandler<>("foobarerror")
                    .handle(mock(IMessageHandlerCallback.class), mock(IMessageContext.class));
            fail();
        } catch (MessageHandlerException e) {
            assertTrue(e.getMessage().contains("foobarerror"));
        }
    }
}
