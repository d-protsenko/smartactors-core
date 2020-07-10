package info.smart_tools.smartactors.core.scope_creation_event_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for ScopeCreationEventHandler
 */
public class ScopeCreationEventHandlerTest {

    @Test
    public void checkHandlerCreation()
            throws InvalidArgumentException{
        IKey key = mock(IKey.class);
        IAction handler = new ScopeCreationEventHandler(key);
        assertNotNull(handler);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnHandlerCreation()
            throws InvalidArgumentException {
        IAction handler = new ScopeCreationEventHandler(null);
        fail();
    }

    @Test
    public void checkHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IAction handler = new ScopeCreationEventHandler(key);
        doNothing().when(scope).setValue(any(IKey.class), any(IStrategyContainer.class));
        handler.execute(scope);
        verify(scope, times(1)).setValue(any(IKey.class), any(IStrategyContainer.class));
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IAction handler = new ScopeCreationEventHandler(key);
        handler.execute(null);
        fail();
    }

    @Test (expected = ActionExecutionException.class)
    public void checkObserverExecuteExceptionOnHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IAction handler = new ScopeCreationEventHandler(key);
        doThrow(ActionExecutionException.class).when(scope).setValue(any(IKey.class), any(IStrategyContainer.class));
        handler.execute(scope);
        fail();
    }
}
