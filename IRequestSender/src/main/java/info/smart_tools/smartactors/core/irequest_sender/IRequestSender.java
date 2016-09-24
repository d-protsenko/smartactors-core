package info.smart_tools.smartactors.core.irequest_sender;

import info.smart_tools.smartactors.core.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface for classes, that should send some requests to other servers
 */
public interface IRequestSender {
    /**
     * Method for initialize request sender
     *
     * @param configuration IObject with configuration of request sender
     */
    void init(final IObject configuration) throws RequestSenderException;

    /**
     * Method for sending request to other server
     *
     * @param request request, that should be sended
     * @return IObject, that contains response from the server
     */
    IObject sendRequest(final IObject request) throws RequestSenderException;
}
