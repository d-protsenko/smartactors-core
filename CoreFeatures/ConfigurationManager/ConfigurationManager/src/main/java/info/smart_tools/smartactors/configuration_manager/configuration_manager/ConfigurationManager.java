package info.smart_tools.smartactors.configuration_manager.configuration_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implementation of {@link IConfigurationManager}
 */
public class ConfigurationManager implements IConfigurationManager {
    private final List<ISectionStrategy> sectionStrategies;
    private final Set<IFieldName> registeredSections;

    /**
     * The constructor.
     */
    public ConfigurationManager() {
        sectionStrategies = new CopyOnWriteArrayList<>();
        registeredSections = new CopyOnWriteArraySet<>();
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
    public void applyConfig(final IObject config)
            throws InvalidArgumentException, ConfigurationProcessingException {
        for (ISectionStrategy sectionStrategy : sectionStrategies) {
            try {
                if (null != config.getValue(sectionStrategy.getSectionName())) {
                    sectionStrategy.onLoadConfig(config);
                }
            } catch (ReadValueException e) {
                throw new ConfigurationProcessingException(
                        MessageFormat.format("Could not read section ''{0}'' from given configuration object.",
                                sectionStrategy.getSectionName()));
            }
        }
    }
}
