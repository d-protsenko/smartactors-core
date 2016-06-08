package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Interface for all wrappers for IMessage
 */
public interface IObjectWrapper {

    /**
     * Get wrapped object
     * @return message
     */
    IObject extractWrapped();

    /**
     * Init wrapper with object
     * @param message init parameter
     */
    void init(IObject message);
}
