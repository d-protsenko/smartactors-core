package info.smart_tools.smartactors.core.configuration_manager;

import info.smart_tools.smartactors.core.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link IConfigurationManager}
 */
public class ConfigurationManager implements IConfigurationManager {
    private final List<ISectionStrategy> sectionStrategies;
    private final Set<IFieldName> registeredSections;

    private IObject currentConfig;
    private boolean isInitialConfigurationApplied;

    /**
     * The constructor.
     */
    public ConfigurationManager() {
        sectionStrategies = new LinkedList<>();
        registeredSections = new HashSet<>();
        currentConfig = null;
        isInitialConfigurationApplied = false;
    }

    @Override
    public void addSectionStrategy(final ISectionStrategy strategy) throws InvalidArgumentException {
        if (null == strategy) {
            throw new InvalidArgumentException("Strategy should not be null.");
        }

        if (!registeredSections.add(strategy.getSectionName())) {
            throw new InvalidArgumentException("Strategy for the section is already registered.");
        }

        sectionStrategies.add(strategy);
    }

    @Override
    public void setInitialConfig(final IObject config) throws InvalidArgumentException, InvalidStateException {
        if (null == config) {
            throw new InvalidArgumentException("Config should not be null.");
        }

        if (isInitialConfigurationApplied) {
            throw new InvalidStateException("Initial configuration is already applied.");
        }

        currentConfig = config;
    }

    @Override
    public IObject getConfig() {
        return currentConfig;
    }

    @Override
    public void configure() throws ConfigurationProcessingException, InvalidStateException {
        if (null == currentConfig) {
            throw new InvalidStateException("Initial configuration is not set.");
        }

        if (isInitialConfigurationApplied) {
            throw new InvalidStateException("Initial configuration is already applied.");
        }

        isInitialConfigurationApplied = true;

        for (ISectionStrategy sectionStrategy : sectionStrategies) {
            sectionStrategy.onLoadConfig(currentConfig);
        }
    }
}
