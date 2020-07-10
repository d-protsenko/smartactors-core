package info.smart_tools.smartactors.https_endpoint.https_request_sender_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.iclient.IClient;
import info.smart_tools.smartactors.https_endpoint.https_request_sender_actor.exception.HttpsRequestSenderActorException;
import info.smart_tools.smartactors.https_endpoint.https_request_sender_actor.wrapper.HttpsRequestSenderActorWrapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

/**
 * Created by sevenbits on 15.10.16.
 */
public class HttpsRequestSenderActor {
    private IFieldName uriFieldName;

    /**
     * Constructor for actor
     */
    public HttpsRequestSenderActor() {
        try {
            uriFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "uri");
        } catch (ResolutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handler of the actor for send response
     *
     * @param message Wrapper of the actor
     * @throws HttpsRequestSenderActorException if there are some problems on sending http request
     */
    public void sendRequest(final HttpsRequestSenderActorWrapper message)
            throws HttpsRequestSenderActorException {
        try {
            if (message.getRequest().getValue(uriFieldName).toString().startsWith("https:")) {
                uriFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "uri");
                if (message.getRequest().getValue(uriFieldName) != null) {
                    IClient client = IOC.resolve(Keys.getKeyByName("getHttpsClient"), message.getRequest());
                    IOC.resolve(Keys.getKeyByName("sendHttpsRequest"), client, message.getRequest());
                }
            }
            if (message.getRequest().getValue(uriFieldName).toString().startsWith("http:")) {
                uriFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "uri");
                if (message.getRequest().getValue(uriFieldName) != null) {
                    IClient client = IOC.resolve(Keys.getKeyByName("getHttpClient"), message.getRequest());
                    IOC.resolve(Keys.getKeyByName("sendHttpRequest"), client, message.getRequest());
                }
            }
        } catch (ResolutionException | ReadValueException | InvalidArgumentException e) {
            throw new HttpsRequestSenderActorException(e);
        }
    }
}
