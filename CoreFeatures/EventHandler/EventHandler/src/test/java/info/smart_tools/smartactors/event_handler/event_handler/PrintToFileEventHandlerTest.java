package info.smart_tools.smartactors.event_handler.event_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IActionTwoArgs;
import info.smart_tools.smartactors.event_handler.event_handler.exception.EventHandlerException;
import info.smart_tools.smartactors.event_handler.event_handler.exception.ExtendedEventHandlerException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrintToFileEventHandlerTest {

    @Before
    public void init()
            throws Exception {

    }

    @Test
    public void should_be_not_null()
            throws Exception {
        IEventHandler handler = new PrintToFileEventHandler("test1", (event, writer) -> {});
        assertNotNull(handler);
        assertNotNull("test1", handler.getEventHandlerKey());

        IActionTwoArgs<IEvent, PrintWriter> testAction = mock(IActionTwoArgs.class);
        handler = new PrintToFileEventHandler(
                "test2", (event, writer) -> {}, new HashMap<Object, Object>(){{put("action", testAction);}}
        );
        assertNotNull(handler);
        assertNotNull("test2", handler.getEventHandlerKey());
        Object removedAction = ((IExtendedEventHandler) handler).removeExecutor("action");
        assertEquals(testAction, removedAction);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_exception_on_creating_with_invalid_executor() {
        IEventHandler handler = new PrintToFileEventHandler(
                "test", (event, writer) -> {}, new HashMap<Object, Object>(){{put("action", "");}}
        );
    }

    @Test
    public void should_call_specific_executor_or_default()
            throws Exception {
//        IAction<IEvent> defaultAction = mock(IAction.class);
//        IAction<IEvent> testAction = mock(IAction.class);
//        IEventHandler handler = new PrintToConsoleEventHandler(
//                "test", defaultAction, new HashMap<Object, Object>(){{put("test", testAction);}}
//        );
//        IEvent eventWithSpecifiedType = mock(IEvent.class);
//        IEvent event = mock(IEvent.class);
//        when(eventWithSpecifiedType.getType()).thenReturn("test");
//        handler.handle(eventWithSpecifiedType);
//        verify(testAction, times(1)).execute(eventWithSpecifiedType);
//        verify(defaultAction, times(0)).execute(eventWithSpecifiedType);
//        handler.handle(event);
//        verify(testAction, times(0)).execute(event);
//        verify(defaultAction, times(1)).execute(event);
//        Object action = ((PrintToConsoleEventHandler) handler).removeExecutor("test");
//        assertEquals(testAction, action);
    }

    @Test (expected = EventHandlerException.class)
    public void should_throw_specified_any_exception_on_executor()
            throws Exception {
        IActionTwoArgs<IEvent, PrintWriter> testAction = mock(IActionTwoArgs.class);
        IEventHandler handler = new PrintToFileEventHandler(
                "test",
                (event, writer) -> {},
                new HashMap<Object, Object>() {{put("test", testAction);}}
        );
        IEvent event = mock(IEvent.class);
        when(event.getType()).thenReturn("test");
        doThrow(new RuntimeException()).when(testAction).execute(any(), any());
        handler.handle(event);
    }

    @Test
    public void should_do_nothing_on_null_event()
            throws Exception {
        IEventHandler handler = new PrintToFileEventHandler("test", (event, writer) -> {});
        handler.handle(null);
    }

    @Test (expected = ExtendedEventHandlerException.class)
    public void should_throw_specified_exception_on_invalid_key()
            throws Exception {
        IEventHandler handler = new PrintToFileEventHandler("test", (event, writer) -> {});
        ((IExtendedEventHandler) handler).addExecutor(5, mock(IActionTwoArgs.class));
    }

    @Test (expected = ExtendedEventHandlerException.class)
    public void should_throw_specified_exception_on_invalid_executor()
            throws Exception {
        IEventHandler handler = new PrintToFileEventHandler("test", (event, writer) -> {});
        ((IExtendedEventHandler) handler).addExecutor("test", 5);
    }
}
