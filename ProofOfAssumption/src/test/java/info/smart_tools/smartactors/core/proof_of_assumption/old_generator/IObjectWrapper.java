package info.smart_tools.smartactors.core.proof_of_assumption.old_generator;

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
     */
    void init(IObject message);
}
