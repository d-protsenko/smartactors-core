package info.smart_tools.smartactors.core.message_processor;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.IPoorAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.imessage_processing_sequence.IMessageProcessingSequence;
import info.smart_tools.smartactors.core.imessage_processing_sequence.exceptions.NoExceptionHandleChainException;
import info.smart_tools.smartactors.core.imessage_receiver.IMessageReceiver;
import info.smart_tools.smartactors.core.imessage_receiver.exception.MessageReceiveException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iresource_source.IResourceSource;
import info.smart_tools.smartactors.core.iresource_source.exceptions.OutOfResourceException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.message_context.IMessageContextContainer;
import info.smart_tools.smartactors.core.message_context.MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link MessageProcessor}.
 */
public class MessageProcessorTest {
    private IQueue<ITask> taskQueueMock;
    private IMessageProcessingSequence messageProcessingSequenceMock;
    private IMessage messageMock;
    private IObject contextMock;
    private IMessageContextContainer messageContextContainerMock;

    @Before
    public void setUp()
            throws Exception {
        taskQueueMock = (IQueue<ITask>) mock(IQueue.class);
        messageProcessingSequenceMock = mock(IMessageProcessingSequence.class);
        messageMock = mock(IMessage.class);
        contextMock = mock(IObject.class);
        messageContextContainerMock = mock(IMessageContextContainer.class);

        Field field = MessageContext.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, messageContextContainerMock);
        field.setAccessible(false);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_queueIsNull()
            throws Exception {
        assertNotNull(new MessageProcessor(null, messageProcessingSequenceMock));
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrow_When_messageProcessingSequenceIsNull()
            throws Exception {
        assertNotNull(new MessageProcessor(taskQueueMock, null));
    }
    @Test
    public void Should_interruptThread_When_putToQueueIsInterrupted()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        doThrow(new InterruptedException()).when(taskQueueMock).put(any());

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));
        assertTrue(Thread.interrupted());
    }

    @Test
    public void Should_workWhenNoExceptionsOccurs()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        IMessageReceiver messageReceiverMock2 = mock(IMessageReceiver.class);
        ArgumentCaptor<IAction> actionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);

        doAnswer(invocation -> {
            verify(messageContextContainerMock).setCurrentContext(contextMock);
            return null;
        }).when(messageReceiverMock1).receive(same(messageMock), any());

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        messageProcessor.execute();
        verify(messageReceiverMock1).receive(same(messageMock), actionArgumentCaptor.capture());

        reset(taskQueueMock, messageProcessingSequenceMock);

        when(messageProcessingSequenceMock.next()).thenReturn(true);
        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock2);
        actionArgumentCaptor.getValue().execute(null);
        verify(messageProcessingSequenceMock).next();
        verify(taskQueueMock).put(same(messageProcessor));

        messageProcessor.execute();
        verifyNoMoreInteractions(messageReceiverMock1);
    }

    @Test
    public void Should_retryWhenOutOfResourcesExceptionOccursAsynchronously()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        OutOfResourceException outOfResourceExceptionMock = mock(OutOfResourceException.class);
        IResourceSource resourceSourceMock = mock(IResourceSource.class);
        ArgumentCaptor<IAction> actionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);
        ArgumentCaptor<IPoorAction> poorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        when(outOfResourceExceptionMock.getSource()).thenReturn(resourceSourceMock);

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        messageProcessor.execute();
        verify(messageReceiverMock1).receive(same(messageMock), actionArgumentCaptor.capture());

        actionArgumentCaptor.getValue().execute(new Exception(new RuntimeException(outOfResourceExceptionMock)));

        verify(resourceSourceMock).onAvailable(poorActionArgumentCaptor.capture());

        verifyNoMoreInteractions(taskQueueMock);
        reset(taskQueueMock);

        poorActionArgumentCaptor.getValue().execute();

        verify(taskQueueMock).put(same(messageProcessor));
    }

    @Test
    public void Should_retryWhenOutOfResourcesExceptionOccursSynchronously()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        OutOfResourceException outOfResourceExceptionMock = mock(OutOfResourceException.class);
        IResourceSource resourceSourceMock = mock(IResourceSource.class);
        ArgumentCaptor<IAction> actionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);
        ArgumentCaptor<IPoorAction> poorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);

        when(outOfResourceExceptionMock.getSource()).thenReturn(resourceSourceMock);

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        doThrow(new MessageReceiveException(outOfResourceExceptionMock)).when(messageReceiverMock1).receive(same(messageMock), any());
        messageProcessor.execute();
        verify(messageReceiverMock1).receive(same(messageMock), actionArgumentCaptor.capture());

        verify(resourceSourceMock).onAvailable(poorActionArgumentCaptor.capture());

        verifyNoMoreInteractions(taskQueueMock);
        reset(taskQueueMock);

        poorActionArgumentCaptor.getValue().execute();

        verify(taskQueueMock).put(same(messageProcessor));
    }

    @Test
    public void Should_throw_WhenExceptionOccursAsynchronouslyAndThereIsNoExceptionalChain()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        ArgumentCaptor<IAction> actionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);
        Throwable exception = new Exception();
        Throwable exception2 = mock(NoExceptionHandleChainException.class);

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        doThrow(exception2).when(messageProcessingSequenceMock).catchException(same(exception));
        messageProcessor.execute();
        verify(messageReceiverMock1).receive(same(messageMock), actionArgumentCaptor.capture());

        try {
            actionArgumentCaptor.getValue().execute(exception);
            fail();
        } catch (ActionExecuteException e) {
            assertSame(exception2, e.getCause());
        }

        verify(messageProcessingSequenceMock).catchException(same(exception));
    }

    @Test
    public void Should_throw_WhenExceptionOccursSynchronouslyAndThereIsNoExceptionalChain()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        Throwable exception = mock(MessageReceiveException.class);
        Throwable exception2 = mock(NoExceptionHandleChainException.class);

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        doThrow(exception).when(messageReceiverMock1).receive(same(messageMock), any());
        doThrow(exception2).when(messageProcessingSequenceMock).catchException(same(exception));

        try {
            messageProcessor.execute();
            fail();
        } catch (TaskExecutionException e) {
            assertSame(exception2, e.getCause());
        }

        verify(messageProcessingSequenceMock).catchException(same(exception));
    }

    @Test
    public void Should_notThrow_WhenExceptionOccursAsynchronouslyAndThereIsExceptionalChain()
            throws Exception {
        IMessageReceiver messageReceiverMock1 = mock(IMessageReceiver.class);
        ArgumentCaptor<IAction> actionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);
        Throwable exception = new Exception();

        MessageProcessor messageProcessor = new MessageProcessor(taskQueueMock, messageProcessingSequenceMock);

        messageProcessor.process(messageMock, contextMock);
        verify(taskQueueMock).put(same(messageProcessor));
        reset(taskQueueMock);

        when(messageProcessingSequenceMock.getCurrentReceiver()).thenReturn(messageReceiverMock1);
        messageProcessor.execute();
        verify(messageReceiverMock1).receive(same(messageMock), actionArgumentCaptor.capture());

        actionArgumentCaptor.getValue().execute(exception);

        verify(messageProcessingSequenceMock).catchException(same(exception));
        verify(taskQueueMock).put(same(messageProcessor));
    }
}
