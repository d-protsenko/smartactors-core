package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Common wrappers interface
 */
public interface IObjectWrapper {
    /**
     * Init wrapper by message, context and response
     * @param iobjects array of {@link IObject}
     */
    void init(IObject ... iobjects);
}
