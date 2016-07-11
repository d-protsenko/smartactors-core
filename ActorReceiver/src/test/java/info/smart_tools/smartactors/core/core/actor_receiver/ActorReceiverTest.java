package info.smart_tools.smartactors.core.core.actor_receiver;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Tests for {@link ActorReceiver}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class})
public class ActorReceiverTest {
    private Queue receiverQueueMock;
    private AtomicBoolean receiverFlag;
    private IMessageReceiver childReceiverMock;
    private IMessageProcessor processorMock;
    private IObject argsMock;
    private IAction callbackMock;

    @Before
    public void setUp()
            throws Exception {
        mockStatic(IOC.class);

        IKey actorReceiverQueueKey = mock(IKey.class);
        IKey actorReceiverBusynessFlagKey = mock(IKey.class);

        receiverQueueMock = mock(Queue.class);
        receiverFlag = new AtomicBoolean(false);
        childReceiverMock = mock(IMessageReceiver.class);
        processorMock = mock(IMessageProcessor.class);
        argsMock = mock(IObject.class);
        callbackMock = mock(IAction.class);

        when(IOC.getKeyForKeyStorage()).thenReturn(mock(IKey.class));
        when(IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_queue")).thenReturn(actorReceiverQueueKey);
        when(IOC.resolve(IOC.getKeyForKeyStorage(), "actor_receiver_busyness_flag")).thenReturn(actorReceiverBusynessFlagKey);

        when(IOC.resolve(actorReceiverQueueKey)).thenReturn(receiverQueueMock);
        when(IOC.resolve(actorReceiverBusynessFlagKey)).thenReturn(receiverFlag);
    }

    @Test(expected = InvalidArgumentException.class)
    public void Should_constructorThrowWhenInitializedWithoutChildReceiver()
            throws Exception {
        assertNotNull(new ActorReceiver(null));
    }

    @Test
    public void Should_enqueueMessageProcessor_When_ActorIsBusy()
            throws Exception {
        ArgumentCaptor<Object[]> arrayCaptor = ArgumentCaptor.forClass(Object[].class);

        receiverFlag.set(true);
        when(receiverQueueMock.isEmpty()).thenReturn(false);

        ActorReceiver actorReceiver = new ActorReceiver(childReceiverMock);

        actorReceiver.receive(processorMock, argsMock, callbackMock);

        verify(receiverQueueMock).add(arrayCaptor.capture());

        assertSame(processorMock, arrayCaptor.getValue()[0]);
        assertSame(argsMock, arrayCaptor.getValue()[1]);
        assertSame(callbackMock, arrayCaptor.getValue()[2]);
    }

    @Test
    public void Should_executeReceiverImmediately_When_ActorIsNotBusy()
            throws Exception {
        receiverFlag.set(false);

        doAnswer(invocationOnMock -> {
            assertTrue(receiverFlag.get());
            return null;
        }).when(childReceiverMock).receive(any(), any(), any());

        when(receiverQueueMock.isEmpty()).thenReturn(true);

        ActorReceiver actorReceiver = new ActorReceiver(childReceiverMock);

        actorReceiver.receive(processorMock, argsMock, callbackMock);

        verify(childReceiverMock).receive(same(processorMock), same(argsMock), same(callbackMock));
    }

    @Test
    public void Should_executeReceiversFromQueue_When_QueueIsNotEmptyAndActorIsNotBusy()
            throws Exception {
        Object[][] processorMocks = new Object[][] {
                {mock(IMessageProcessor.class), mock(IObject.class), mock(IAction.class)},
                {mock(IMessageProcessor.class), mock(IObject.class), mock(IAction.class)},
        };

        receiverFlag.set(false);

        doAnswer(invocationOnMock -> {
            assertTrue(receiverFlag.get());
            return null;
        }).when(childReceiverMock).receive(any(), any(), any());

        when(receiverQueueMock.isEmpty())
            .thenAnswer(invocationOnMock -> {
                assertFalse(receiverFlag.get());
                return false;
            })
            .thenAnswer(invocationOnMock -> {
                assertFalse(receiverFlag.get());
                return true;
            });

        when(receiverQueueMock.poll())
                .thenReturn(processorMocks[0])
                .thenReturn(processorMocks[1])
                .thenReturn(null);

        ActorReceiver actorReceiver = new ActorReceiver(childReceiverMock);

        actorReceiver.receive(processorMock, argsMock, callbackMock);

        verify(childReceiverMock).receive(processorMock, argsMock, callbackMock);
        verify(childReceiverMock).receive((IMessageProcessor) processorMocks[0][0], (IObject) processorMocks[0][1], (IAction) processorMocks[0][2]);
        verify(childReceiverMock).receive((IMessageProcessor) processorMocks[1][0], (IObject) processorMocks[1][1], (IAction) processorMocks[1][2]);
    }
}
