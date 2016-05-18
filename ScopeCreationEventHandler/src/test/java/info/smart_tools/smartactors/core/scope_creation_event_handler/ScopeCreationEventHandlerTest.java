package info.smart_tools.smartactors.core.scope_creation_event_handler;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobserver.IObserver;
import info.smart_tools.smartactors.core.iobserver.exception.ObserverExecuteException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for ScopeCreationEventHandler
 */
public class ScopeCreationEventHandlerTest {

    @Test
    public void checkHandlerCreation() {
        IKey key = mock(IKey.class);
        IObserver handler = new ScopeCreationEventHandler(key);
        assertNotNull(handler);
    }

    @Test (expected = IllegalArgumentException.class)
    public void checkIllegalExceptionOnHandlerCreation() {
        IObserver handler = new ScopeCreationEventHandler(null);
        fail();
    }

    @Test
    public void checkHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IObserver handler = new ScopeCreationEventHandler(key);
        doNothing().when(scope).setValue(any(IKey.class), any(IStrategyContainer.class));
        handler.execute(scope);
        verify(scope, times(1)).setValue(any(IKey.class), any(IStrategyContainer.class));
    }

    @Test (expected = IllegalArgumentException.class)
    public void checkIllegalArgumentExceptionOnHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IObserver handler = new ScopeCreationEventHandler(key);
        handler.execute(null);
        fail();
    }

    @Test (expected = ObserverExecuteException.class)
    public void checkObserverExecuteExceptionOnHandlerExecution()
            throws Exception {
        IKey key = mock(IKey.class);
        IScope scope = mock(IScope.class);
        IObserver handler = new ScopeCreationEventHandler(key);
        doThrow(ObserverExecuteException.class).when(scope).setValue(any(IKey.class), any(IStrategyContainer.class));
        handler.execute(scope);
        fail();
    }
}
