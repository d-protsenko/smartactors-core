package info.smart_tools.smartactors.endpoint.response_strategy;

import info.smart_tools.smartactors.endpoint.interfaces.ichannel_handler.IChannelHandler;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse.IResponse;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;

/**
 * Response strategy that sends a response to a request received by endpoint.
 */
public class EndpointResponseStrategy implements IResponseStrategy {
    @Override
    public void sendResponse(final IObject environment) throws ResponseException {
        try {
            // Most of this code is copy-pasted from old ResponseSenderActor.
            IFieldName responseFieldName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "response");
            IFieldName contextFieldName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "context");
            IFieldName httpResponseIsSentFieldName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "sendResponseOnChainEnd");
            IFieldName endpointName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "endpointName");
            IFieldName channelFieldName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "channel");

            IObject responseIObject = (IObject) environment.getValue(responseFieldName);
            IObject contextIObject = (IObject) environment.getValue(contextFieldName);

            IChannelHandler channelHandler = (IChannelHandler) contextIObject.getValue(channelFieldName);

            IResponse response = IOC.resolve(Keys.resolveByName(IResponse.class.getCanonicalName()));
            IResponseContentStrategy contentStrategy =
                    IOC.resolve(Keys.resolveByName(IResponseContentStrategy.class.getCanonicalName()), environment);
            contentStrategy.setContent(responseIObject, response);

            IResponseSender sender = IOC.resolve(Keys.resolveByName(IResponseSender.class.getCanonicalName()),
                    IOC.resolve(Keys.resolveByName("http_request_key_for_response_sender"), environment),
                    contextIObject.getValue(endpointName));
            sender.send(response, environment, channelHandler);
            contextIObject.setValue(httpResponseIsSentFieldName, true);
        } catch (Exception e) {
            throw new ResponseException(e);
        }
    }
}
