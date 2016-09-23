package info.smart_tools.smartactors.core.message_processor;

import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FinalTask}.
 */
public class FinalTaskTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new FieldName((String) a[0]);
                            } catch (Throwable e) {
                                throw new RuntimeException("Could not create new instance of FieldName", e);
                            }
                        }
                )
        );
    }

    @Test
    public void checkCreation()
            throws Exception {
        IObject env = mock(IObject.class);
        ITask task = new FinalTask(env);
        assertNotNull(task);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreation()
            throws Exception {
        new FinalTask(null);
        fail();
    }

    @Test (expected = ResolutionException.class)
    public void checkResolutionExceptionOnCreation()
            throws Exception {
        IObject env = mock(IObject.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                new ResolveByNameIocStrategy((a)-> {
                    throw new RuntimeException();
                })
        );
        new FinalTask(env);
        fail();
    }

    @Test
    public void checkTaskExecution()
            throws Exception {
        IObject env = mock(IObject.class);
        IObject context = mock(IObject.class);
        List<IAction> finalActions = new ArrayList<>();
        when(env.getValue(new FieldName("context"))).thenReturn(null).thenReturn(context);
        when(context.getValue(new FieldName("finalActions"))).thenReturn(null).thenReturn(finalActions);
        Checker checker = new Checker();
        IAction action = new IAction() {
            @Override
            public void execute(Object actingObject) throws ActionExecuteException, InvalidArgumentException {
                checker.setChecked(true);
            }
        };
        finalActions.add(action);
        ITask task = new FinalTask(env);

        // context is null
        task.execute();

        // finalActions is null
        task.execute();

        // finalAction contains one action
        task.execute();
        assertEquals(checker.isChecked(), true);
    }

    @Test
    public void checkContinueExecutionOnActionException() throws Exception {
        IObject env = mock(IObject.class);
        IObject context = mock(IObject.class);
        List<IAction> finalActions = new ArrayList<IAction>();
        when(env.getValue(new FieldName("context"))).thenReturn(context);

        Checker checker1 = new Checker();
        Checker checker2 = new Checker();
        IAction action1 = new IAction() {
            @Override
            public void execute(Object actingObject) throws ActionExecuteException, InvalidArgumentException {
                checker1.setChecked(true);
                throw new ActionExecuteException("something");
            }
        };
        IAction action2 = new IAction() {
            @Override
            public void execute(Object actingObject) throws ActionExecuteException, InvalidArgumentException {
                checker2.setChecked(true);
            }
        };
        finalActions.add(action1);
        finalActions.add(action2);
        when(context.getValue(new FieldName("finalActions"))).thenReturn(finalActions);
        ITask task = new FinalTask(env);

        // finalAction contains one action
        task.execute();
        assertEquals(checker1.isChecked(), true);
        assertEquals(checker2.isChecked(), true);
    }
}

class Checker {

    private boolean checked = false;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
