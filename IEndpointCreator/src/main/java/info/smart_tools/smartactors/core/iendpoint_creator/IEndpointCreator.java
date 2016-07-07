package info.smart_tools.smartactors.core.iendpoint_creator;

import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Endpoint creator in factory method pattern.
 */
public interface IEndpointCreator {

    /**
     * Creates endpoint.
     * @param params ...
     * @return endpoint constructed by parameters.
     */
    IAsyncService create(IObject params);

}