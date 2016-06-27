package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Common wrappers interface
 */
public interface IObjectWrapper {

    /**
     * Get IObject message
     * @return message
     */
    IObject getMessage();

    /**
     * Get IObject context
     * @return context
     */
    IObject getContext();

    /**
     * Get IObject response
     * @return response
     */
    IObject getResponse();

    /**
     * Init wrapper by message, context and response
     * @param message message
     * @param context context
     * @param response response
     */
    void init(IObject message, IObject context, IObject response);
}
