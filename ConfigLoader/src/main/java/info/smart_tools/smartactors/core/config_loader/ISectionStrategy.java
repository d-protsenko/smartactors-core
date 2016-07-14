package info.smart_tools.smartactors.core.config_loader;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Strategy that defines methods for processing of some section of server configuration.
 */
public interface ISectionStrategy {
    /**
     * Called when configuration is loaded.
     *
     * @param config    the root configuration object
     * @throws ReadValueException if error occurs accessing configuration object
     * @see info.smart_tools.smartactors.core.config_loader
     */
    void onLoadConfig(final IObject config) throws ReadValueException;

    /**
     * Return name of the configuration section processed by this strategy.
     *
     * @return name of the configuration section
     */
    IFieldName getSectionName();

    // TODO: Add methods for modification of config
}
