package info.smart_tools.smartactors.core.strategy_provider_container;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.strategy_provider.IStrategyProviderContainer;
import info.smart_tools.smartactors.core.strategy_provider.exception.StrategyProviderException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IStrategyProviderContainer}
 */
public class StrategyProviderContainer implements IStrategyProviderContainer {

    /**
     * Local storage of {@link IStrategyFactory} instances by unique identifier
     */
    private Map<Object, IStrategyFactory> strategyFactories = new HashMap<Object, IStrategyFactory>();

    /**
     * Create new instance of specific {@link IResolveDependencyStrategy}
     * @param key unique identifier of {@link IResolveDependencyStrategy}
     * @param args parameters needed for creation new instance of {@link IResolveDependencyStrategy}
     * @return new instance of {@link IResolveDependencyStrategy}
     * @throws StrategyProviderException if any errors occurred
     */
    public IResolveDependencyStrategy createStrategy(final Object key, final Object args)
            throws StrategyProviderException {
        try {
            return strategyFactories.get(key).createStrategy(args);
        } catch (Exception e) {
            throw new StrategyProviderException("Strategy creation failed.", e);
        }
    }

    /**
     * Add new instance of {@link IStrategyFactory} to the Strategy Provider local storage
     * @param key unique identifier for {@link IStrategyFactory}
     * @param factory instance of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    public void addStrategyFactory(final Object key, final IStrategyFactory factory)
            throws StrategyProviderException {
        strategyFactories.put(key, factory);
    }

    /**
     * Remove instance of {@link IStrategyFactory} from Strategy Provider local storage
     * @param key unique identifier of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    public void removeStrategyFactory(final Object key)
            throws StrategyProviderException {
        strategyFactories.remove(key);
    }
}
