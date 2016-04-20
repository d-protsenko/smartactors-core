package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.iobject.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_provider.StrategyProvider;

/**
 * Implementation of {@link IContainer}
 * <pre>
 * Implementation features:
 * - support scopes
 * </pre>
 */
public class Container implements IContainer {

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


    /**
     * Resolve dependency by {@link IObject}
     * @param obj instance of {@link IObject} that contains needed parameters for resolve dependency
     * @param <T> type of object
     * @return instance of object
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    public <T> T resolve(final IObject obj) throws ResolutionException {
        try {
            IStrategyContainer strategyContainer = (IStrategyContainer) ScopeProvider.getCurrentScope().getValue(STRATEGY_CONTAINER_KEY);
            Object objId = obj.getValue(new FieldName(CLASS_ID_KEY));
            Object[] args = (Object[]) obj.getValue(new FieldName(ARGS_KEY));
            IResolveDependencyStrategy strategy = strategyContainer.resolve(objId);
            return (T) strategy.resolve(args);
        } catch (Exception e) {
            throw new ResolutionException("Resolution of dependency failed.");
        }
    }

    /**
     * Register new instance of {@link IResolveDependencyStrategy} to the instance of {@link IStrategyContainer}
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @throws RegistrationException if dependency registration is impossible
     */
    public void register(final IObject obj) throws RegistrationException {
        try {
            Object objId = obj.getValue(new FieldName(CLASS_ID_KEY));
            Object strategyId = obj.getValue(new FieldName(STRATEGY_ID_KEY));
            Object strategyArgs = obj.getValue(new FieldName(STRATEGY_ARGS_KEY));
            IStrategyContainer strategyContainer = (IStrategyContainer) ScopeProvider.getCurrentScope().getValue(STRATEGY_CONTAINER_KEY);
            IResolveDependencyStrategy strategy = StrategyProvider.createStrategy(strategyId, strategyArgs);
            strategyContainer.register(objId, strategy);
        } catch (Exception e) {
            throw new RegistrationException("Registration of dependency failed.", e);
        }
    }
}
