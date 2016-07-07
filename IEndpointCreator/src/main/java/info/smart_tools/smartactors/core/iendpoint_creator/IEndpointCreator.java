package info.smart_tools.smartactors.core.iendpoint_creator;

import info.smart_tools.smartactors.core.IObject;
import info.smart_tools.smartactors.core.services.AsyncService;

/**
 * Endpoint creator in factory method pattern.
 */
public interface IEndpointCreator {

    /**
     * Creates endpoint.
     * @param params ...
     * @return endpoint constructed by parameters.
     */
    AsyncService create(IObject params);

}