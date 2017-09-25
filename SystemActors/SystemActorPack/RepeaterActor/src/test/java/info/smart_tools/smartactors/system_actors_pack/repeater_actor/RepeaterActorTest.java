package info.smart_tools.smartactors.system_actors_pack.repeater_actor;

import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.system_actors_pack.repeater_actor.wrapper.IRepeatRequest;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Test for {@link RepeaterActor}.
 */
public class RepeaterActorTest {
    private IMessageProcessingSequence sequenceMock;
    private IRepeatRequest requestMock;

    @Before
    public void setUp()
            throws Exception {
        sequenceMock = mock(IMessageProcessingSequence.class);
        requestMock = mock(IRepeatRequest.class);

        when(requestMock.getSequence()).thenReturn(sequenceMock);
    }

    @Test
    public void Should_repeaterRepeatChainWhenRequired()
            throws Exception {
        when(sequenceMock.getCurrentLevel()).thenReturn(42);
        when(requestMock.getRepeatCondition()).thenReturn(true);

        RepeaterActor actor = new RepeaterActor();

        actor.handle(requestMock);

        verify(sequenceMock).getCurrentLevel();
        verify(sequenceMock).goTo(42, 0);

        verifyNoMoreInteractions(sequenceMock);
    }

    @Test
    public void Should_repeaterNotRepeatChainWhenNotRequired()
            throws Exception {
        when(requestMock.getRepeatCondition()).thenReturn(false);

        RepeaterActor actor = new RepeaterActor();

        actor.handle(requestMock);

        verifyNoMoreInteractions(sequenceMock);
    }
}
