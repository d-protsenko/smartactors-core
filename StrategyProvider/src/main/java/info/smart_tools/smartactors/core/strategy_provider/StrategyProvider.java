package info.smart_tools.smartactors.core.strategy_provider;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.strategy_provider.exception.StrategyProviderException;

/**
 * Realization of strategy storage by ServiceLocator pattern
 */
public final class StrategyProvider {

    /**
     * Private default constructor
     */
    private StrategyProvider() {
    }

    /**
     * Implementation of {@link IStrategyProviderContainer}.
     * Must be initialized before StrategyProvider will be used.
     * Initialization possible only with using java reflection API
     * Example:
     * <pre>
     * {@code
     * Field field = StrategyProvider.class.getDeclaredField("container");
     * field.setAccessible(true);
     * field.set(null, new Object());
     * field.setAccessible(false);
     * }
     * </pre>
     */
    private static IStrategyProviderContainer container;

    /**
     * Create new instance of specific {@link IResolveDependencyStrategy}
     * @param key unique identifier of {@link IResolveDependencyStrategy}
     * @param args parameters needed for creation new instance of {@link IResolveDependencyStrategy}
     * @return new instance of {@link IResolveDependencyStrategy}
     * @throws StrategyProviderException if any errors occurred
     */
    public static IResolveDependencyStrategy createStrategy(final Object key, final Object ... args)
            throws StrategyProviderException {

        return container.createStrategy(key, args);
    }

    /**
     * Add new instance of {@link IStrategyFactory} to the Strategy Provider local storage
     * @param key unique identifier for {@link IStrategyFactory}
     * @param factory instance of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    public static void addStrategyFactory(final Object key, final IStrategyFactory factory)
            throws StrategyProviderException {
        container.addStrategyFactory(key, factory);
    }

    /**
     * Remove instance of {@link IStrategyFactory} from Strategy Provider local storage
     * @param key unique identifier of {@link IStrategyFactory}
     * @throws StrategyProviderException if any errors occurred
     */
    public static void removeStrategyFactory(final Object key)
            throws StrategyProviderException {
        container.removeStrategyFactory(key);
    }
}
