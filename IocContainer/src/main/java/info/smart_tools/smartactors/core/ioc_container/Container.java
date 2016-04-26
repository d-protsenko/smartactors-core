package info.smart_tools.smartactors.core.ioc_container;

import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.IKey;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

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

    /**
     * Resolve dependency by given given {@link IKey} instance and args
     * @param key instance of {@link IKey}
     * @param args needed parameters for resolve dependency
     * @param <T> type of class for resolution
     * @return instance of class with classId identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    public <T> T resolve(final IKey<T> key, final Object ... args)
            throws ResolutionException {
        try {
            IStrategyContainer strategyContainer = (IStrategyContainer) ScopeProvider.getCurrentScope().getValue(STRATEGY_CONTAINER_KEY);
            IResolveDependencyStrategy strategy = strategyContainer.resolve(key);
            return (T) strategy.resolve(args);
        } catch (Exception e) {
            throw new ResolutionException("Resolution of dependency failed.");
        }
    }

    /**
     * Register new dependency by instance of {@link IKey}
     * @param key instance of {@link IKey}
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws RegistrationException when registration is impossible because of any error
     */
    public void register(final IKey key, final IResolveDependencyStrategy strategy)
            throws RegistrationException {
        try {
            IStrategyContainer strategyContainer = (IStrategyContainer) ScopeProvider.getCurrentScope().getValue(STRATEGY_CONTAINER_KEY);
            strategyContainer.register(key, strategy);
        } catch (Exception e) {
            throw new RegistrationException("Registration of dependency failed.", e);
        }
    }
}
