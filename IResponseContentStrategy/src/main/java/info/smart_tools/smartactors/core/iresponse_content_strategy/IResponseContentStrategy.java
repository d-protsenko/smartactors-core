package info.smart_tools.smartactors.core.iresponse_content_strategy;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iresponse.IResponse;

/**
 * Interface for strategy, that should set content of the response
 */
public interface IResponseContentStrategy {
    /**
     * Method for setting content of the response, from {@link IObject} response
     * @param responseObject IObject from environment
     * @param response
     */
    void setContent(final IObject responseObject, IResponse response);
}
