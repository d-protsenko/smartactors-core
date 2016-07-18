package info.smart_tools.smartactors.core.iresponse_environment_strategy;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iresponse.IResponse;

/**
 * Interface for setting environment to response
 */
public interface IResponseEnvironmentStrategy {
    /**
     * Method for setting environment to response
     * @param environment Environment object from message processor
     * @param response
     */
    void setEnvironment(final IObject environment, IResponse response);
}
