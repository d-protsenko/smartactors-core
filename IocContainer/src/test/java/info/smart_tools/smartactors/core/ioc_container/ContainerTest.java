package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.iobject.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.scope_provider.exception.ScopeProviderException;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class ContainerTest {

    /** Key for getting instance of {@link IStrategyContainer} from current scope */
    private static final String STRATEGY_CONTAINER_KEY = "strategy_container";
    /** Key for getting class_id from {@link IObject} */
    private static final String CLASS_ID_KEY = "class_id";
    /** Key for getting args from {@link IObject} */
    private static final String ARGS_KEY = "args";
    /** Key for getting strategy_id from {@link IObject} */
    private static final String STRATEGY_ID_KEY = "strategy_id";
    /** Key for getting strategy_args from {@link IObject} */
    private static final String STRATEGY_ARGS_KEY = "strategy_args";


    @Test
    public void checkResolve()
            throws ReadValueException, ChangeValueException,
            ResolutionException,
            ScopeProviderException, ScopeException {
        Container container = new Container();
        IObject obj = mock(IObject.class);
        IStrategyContainer strategyContainer = mock(IStrategyContainer.class);
        when(obj.getValue(new FieldName(CLASS_ID_KEY))).thenReturn("test.class.id");
        when(ScopeProvider.getCurrentScope().getValue(STRATEGY_CONTAINER_KEY)).thenReturn(strategyContainer);
        container.resolve(obj);
    }
}
