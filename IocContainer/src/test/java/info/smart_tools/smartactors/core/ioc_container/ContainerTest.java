package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import org.junit.Test;

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
    public void checkResolve() {
    }
}
