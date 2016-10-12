package info.smart_tools.smartactors.http_endpoint.http_request_sender_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.iclient.IClient;
import info.smart_tools.smartactors.http_endpoint.http_request_sender_actor.exception.HttpRequestSenderActorException;
import info.smart_tools.smartactors.http_endpoint.http_request_sender_actor.wrappers.HttpRequestSenderActorWrapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Actor for sending request by http
 */
public class HttpRequestSenderActor {
    IFieldName uriFieldName;

    /**
     * Constructor for actor
     */
    public HttpRequestSenderActor() {
    }

    /**
     * Handler of the actor for send response
     *
     * @param message Wrapper of the actor
     * @throws HttpRequestSenderActorException if there are some problems on sending http request
     */
    public void sendRequest(final HttpRequestSenderActorWrapper message)
            throws HttpRequestSenderActorException {
        try {
            uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
            if (message.getRequest().getValue(uriFieldName) != null) {
                IClient client = IOC.resolve(Keys.getOrAdd("getHttpClient"), message.getRequest());
                IOC.resolve(Keys.getOrAdd("sendHttpRequest"), client, message.getRequest());
            }
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new HttpRequestSenderActorException(e);
        }
    }

}
