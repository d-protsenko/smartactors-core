package info.smart_tools.smartactors.actors.response_sender_actor;

import info.smart_tools.smartactors.actors.response_sender_actor.wrapper.ResponseMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse.IResponse;
import info.smart_tools.smartactors.core.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.core.iresponse_environment_strategy.IResponseEnvironmentStrategy;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

public class ResponseSenderActor {
    public ResponseSenderActor() {
    }

    public void sendResponse(final ResponseMessage message) throws ReadValueException, ResolutionException, SerializeException {
        IResponse response = IOC.resolve(Keys.getOrAdd(IResponse.class.getCanonicalName()));
        IResponseContentStrategy contentStrategy =
                IOC.resolve(Keys.getOrAdd(IResponseContentStrategy.class.getCanonicalName()), message.getEnvironment());
        contentStrategy.setContent(message.getResponse(), response);
        IResponseEnvironmentStrategy environmentStrategy =
                IOC.resolve(Keys.getOrAdd(IResponseEnvironmentStrategy.class.getCanonicalName()),
                        message.getEnvironment());
        environmentStrategy.setEnvironment(message.getEnvironment(), response);
        IResponseSender sender = IOC.resolve(Keys.getOrAdd(IResponseSender.class.getCanonicalName()),
                message.getProtocol());
        sender.send(response, message.getChannelHandlerContext());
    }
}