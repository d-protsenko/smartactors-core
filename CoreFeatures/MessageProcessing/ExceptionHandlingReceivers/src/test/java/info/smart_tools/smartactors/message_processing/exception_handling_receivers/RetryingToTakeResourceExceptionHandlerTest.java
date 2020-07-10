package info.smart_tools.smartactors.message_processing.exception_handling_receivers;

import info.smart_tools.smartactors.base.interfaces.iaction.IActionNoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.interfaces.iresource_source.IResourceSource;
import info.smart_tools.smartactors.base.interfaces.iresource_source.exceptions.OutOfResourceException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Test for {@link RetryingToTakeResourceExceptionHandler}.
 */
public class RetryingToTakeResourceExceptionHandlerTest extends ExceptionHandlingReceiverTest {
    private OutOfResourceException outOfResourceExceptionMock;
    private IResourceSource resourceSourceMock;
    private ArgumentCaptor<IActionNoArgs> callbackCaptor;
    private IMessageProcessingSequence sequenceMock;

    @Before
    public void setUp()
            throws Exception {
        super.setUp();
        outOfResourceExceptionMock = mock(OutOfResourceException.class);
        resourceSourceMock = mock(IResourceSource.class);

        when(outOfResourceExceptionMock.getSource()).thenReturn(resourceSourceMock);

        callbackCaptor = ArgumentCaptor.forClass(IActionNoArgs.class);

        sequenceMock = mock(IMessageProcessingSequence.class);
    }

    @Test
    public void Should_enqueueACallbackToResourceSource()
            throws Exception {
        AsynchronousOperationException asynchronousOperationException = mock(AsynchronousOperationException.class);

        when(messageProcessorMock.getSequence()).thenReturn(sequenceMock);

        when(contextMock.getValue(same(exceptionFieldName))).thenReturn(outOfResourceExceptionMock);
        when(contextMock.getValue(same(causeLevelFieldName))).thenReturn(135);
        when(contextMock.getValue(same(causeStepFieldName))).thenReturn(123);

        IMessageReceiver receiver = new RetryingToTakeResourceExceptionHandler();

        receiver.receive(messageProcessorMock);

        verify(sequenceMock).goTo(eq(135), eq(123));
        verify(resourceSourceMock).onAvailable(callbackCaptor.capture());

        callbackCaptor.getValue().execute();
        verify(messageProcessorMock).continueProcess(null);

        reset(messageProcessorMock);
        doThrow(asynchronousOperationException).when(messageProcessorMock).continueProcess(null);

        try {
            callbackCaptor.getValue().execute();
            fail();
        } catch (ActionExecutionException e) {
            assertSame(asynchronousOperationException, e.getCause());
        }

        receiver.dispose();
    }

    @Test
    public void Should_wrapCaughtExceptions()
            throws Exception {
        ReadValueException readValueException = mock(ReadValueException.class);
        when(contextMock.getValue(any())).thenThrow(readValueException);

        IMessageReceiver receiver = new RetryingToTakeResourceExceptionHandler();

        try {
            receiver.receive(messageProcessorMock);
            fail();
        } catch (MessageReceiveException e) {
            assertSame(readValueException, e.getCause());
        }
    }
}