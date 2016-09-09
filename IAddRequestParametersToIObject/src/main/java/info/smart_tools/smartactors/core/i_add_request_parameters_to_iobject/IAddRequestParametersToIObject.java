package info.smart_tools.smartactors.core.i_add_request_parameters_to_iobject;

import info.smart_tools.smartactors.core.i_add_request_parameters_to_iobject.exception.AddRequestParametersToIObjectException;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Interface for classes, that extracting parameters from request to IObject
 */
public interface IAddRequestParametersToIObject {

    void extract(final IObject message, final Object request) throws AddRequestParametersToIObjectException;
}
