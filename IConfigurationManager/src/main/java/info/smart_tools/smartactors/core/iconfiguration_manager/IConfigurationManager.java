package info.smart_tools.smartactors.core.iconfiguration_manager;

import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.invalid_state_exception.InvalidStateException;
import info.smart_tools.smartactors.core.iobject.IObject;

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
     * Set initial configuration object.
     *
     * @param config    the initial configuration object
     * @throws InvalidArgumentException if {@code config} is {@code null}
     * @throws InvalidStateException if initial configuration is already parsed (by {@link #configure()})
     */
    void setInitialConfig(final IObject config) throws InvalidArgumentException, InvalidStateException;

    /**
     * Get the current configuration object.
     *
     * @return the current configuration object
     */
    IObject getConfig();

    /**
     * Apply initial configuration (call {@link ISectionStrategy#onLoadConfig(IObject)} of all strategies added using {@link
     * #addSectionStrategy(ISectionStrategy)}).
     *
     * @throws ConfigurationProcessingException if error occurs processing some section of configuration object
     * @throws InvalidStateException if initial configuration is not set
     */
    void configure() throws ConfigurationProcessingException, InvalidStateException;
}
