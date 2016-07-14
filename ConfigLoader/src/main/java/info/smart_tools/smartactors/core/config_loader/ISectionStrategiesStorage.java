package info.smart_tools.smartactors.core.config_loader;

import java.util.List;

/**
 * Stores instances of {@link ISectionStrategy}.
 */
public interface ISectionStrategiesStorage {
    /**
     * Register the {@link ISectionStrategy}.
     *
     * @param strategy    the strategy to register
     */
    void register(final ISectionStrategy strategy);

    /**
     * List of all strategies registered at this moment in the order they were registered.
     *
     * @return list of all strategies registered at this moment
     */
    List<ISectionStrategy> getRegistered();
}
