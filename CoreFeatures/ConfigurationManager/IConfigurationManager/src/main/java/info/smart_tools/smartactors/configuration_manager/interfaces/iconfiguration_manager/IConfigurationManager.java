package info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager;

import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Configuration manager stores current configuration object, strategies for processing of configuration sections, applies the strategies.
 */
public interface IConfigurationManager {
    /**
     * Add a strategy for processing of specific section of configuration.
     *
     * @param strategy    the strategy
     * @throws InvalidArgumentException if {@code strategy} is {@code null}
     * @throws InvalidArgumentException if there already is a strategy for the same section
     * @see ISectionStrategy#getSectionName()
     */
    void addSectionStrategy(final ISectionStrategy strategy) throws InvalidArgumentException;

    /**
     * Apply given configuration object.
     *
     * @param config    the initial configuration object
     * @throws InvalidArgumentException if {@code config} is {@code null}
     * @throws ConfigurationProcessingException if any error occurs processing given configuration
     */
    void applyConfig(final IObject config) throws InvalidArgumentException, ConfigurationProcessingException;
}
