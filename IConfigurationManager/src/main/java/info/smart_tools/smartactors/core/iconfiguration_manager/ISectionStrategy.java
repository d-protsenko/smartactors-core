package info.smart_tools.smartactors.core.iconfiguration_manager;

import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Strategy that defines methods for processing of some section of server configuration.
 */
public interface ISectionStrategy {
    /**
     * Called when configuration is loaded.
     *
     * @param config    the root configuration object
     * @throws ConfigurationProcessingException if any error occurs loading the configuration
     * @see info.smart_tools.smartactors.core.iconfiguration_manager
     */
    void onLoadConfig(final IObject config) throws ConfigurationProcessingException;

    /**
     * Return name of the configuration section processed by this strategy.
     *
     * @return name of the configuration section
     */
    IFieldName getSectionName();

    // TODO: Add methods for modification of config
}
