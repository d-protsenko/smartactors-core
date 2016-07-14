package info.smart_tools.smartactors.core.config_loader.impl;

import info.smart_tools.smartactors.core.config_loader.ISectionStrategiesStorage;
import info.smart_tools.smartactors.core.config_loader.ISectionStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of {@link ISectionStrategiesStorage}.
 */
public class SectionStrategiesStorage implements ISectionStrategiesStorage {
    private final List<ISectionStrategy> strategies;

    /**
     * The constructor.
     */
    public SectionStrategiesStorage() {
        strategies = new CopyOnWriteArrayList<>();
    }

    @Override
    public void register(final ISectionStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public List<ISectionStrategy> getRegistered() {
        return new ArrayList<>(strategies);
    }
}
