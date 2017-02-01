package info.smart_tools.smartactors.http_endpoint.http_request_sender_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.endpoint.interfaces.iclient.IClient;
import info.smart_tools.smartactors.http_endpoint.http_request_sender_actor.exception.HttpRequestSenderActorException;
import info.smart_tools.smartactors.http_endpoint.http_request_sender_actor.wrappers.HttpRequestSenderActorWrapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.List;

/**
 * Actor for sending request by http
 * <p>
 * Request example
 * <pre>
 * "request": {
 * "uuid": "some_uuid",
 * "uri": "http://uri.for.request/something",
 * "method": "POST",
 * "timeout": 100, after this timeout to "exceptionalMessageMapId" will send message with full request
 * "exceptionalMessageMapId": "SelectChain",
 * "messageMapId": "sendRequest", start chain for response
 * "content": {
 * "hello": "world"
 * }
 * }
 * </pre>
 */
public class HttpRequestSenderActor {

    private IFieldName uriFieldName;
    private IFieldName contextFieldName;
    private IFieldName headersFieldName;
    private IFieldName cookiesFieldName;
    private IFieldName channelFieldName;
    private IFieldName httpResponseIsSentFieldName;
    private IFieldName responseFieldName;
    private IFieldName configFieldName;
    private IFieldName requestFieldName;

    /**
     * Constructor for actor
     */
    public HttpRequestSenderActor() {
        try {
            uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
            contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
            requestFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "request");
            headersFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "headers");
            cookiesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "cookies");
            channelFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "channel");
            httpResponseIsSentFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sendResponseOnChainEnd");
            responseFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sendResponseOnChainEnd");
            configFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sendResponseOnChainEnd");
        } catch (ResolutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handler of the actor for send response
     *
     * @param wrapper Wrapper of the actor
     * @throws HttpRequestSenderActorException if there are some problems on sending http request
     */
    public void sendRequest(final HttpRequestSenderActorWrapper wrapper)
            throws HttpRequestSenderActorException {
        try {
            IObjectWrapper objectWrapper = (IObjectWrapper) wrapper;
            IObject context = objectWrapper.getEnvironmentIObject(contextFieldName);
            Object cameRequest = objectWrapper.getEnvironmentIObject(requestFieldName);
            List<IObject> headers = (List<IObject>) context.getValue(headersFieldName);
            List<IObject> cookies = (List<IObject>) context.getValue(cookiesFieldName);
            IChannelHandler channel = (IChannelHandler) context.getValue(channelFieldName);
            IObject response = objectWrapper.getEnvironmentIObject(responseFieldName);
            IObject config = objectWrapper.getEnvironmentIObject(configFieldName);
            context.setValue(httpResponseIsSentFieldName,true);
            IObject request = wrapper.getRequest();
            if (request.getValue(uriFieldName).toString().startsWith("http:")) {
                uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
                if (request.getValue(uriFieldName) != null) {
                    IClient client = IOC.resolve(Keys.getOrAdd("getHttpClient"), request, cameRequest, headers, cookies, channel, response, config);
                    IOC.resolve(Keys.getOrAdd("sendHttpRequest"), client, request);
                }
            }
        } catch (ResolutionException | ReadValueException | InvalidArgumentException | ChangeValueException e) {
            throw new HttpRequestSenderActorException(e);
        }
    }

}
