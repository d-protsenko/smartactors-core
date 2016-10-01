package info.smart_tools.smartactors.actor.client;

import info.smart_tools.smartactors.actor.client.exception.RequestSenderActorException;
import info.smart_tools.smartactors.actor.client.wrapper.ClientActorMessage;
import info.smart_tools.smartactors.core.irequest_sender.IRequestSender;
import info.smart_tools.smartactors.core.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Actor for sending requests to other servers
 */
public class ClientActor {
    /**
     * Constructor for actor
     */
    public ClientActor() {
    }

    /**
     * Handler of the actor for send response
     *
     * @param message Wrapper of the actor
     * @throws RequestSenderActorException if there are some problems on sending response
     */
    public void sendRequest(final ClientActorMessage message)
            throws RequestSenderActorException {
        IRequestSender requestSender = null;
        try {
            requestSender = IOC.resolve(Keys.getOrAdd(IRequestSender.class.getCanonicalName()), message.getRequest());
            requestSender.sendRequest(message.getRequest());
            IOC.resolve(Keys.getOrAdd("stopHttpClient"), requestSender);
        } catch (RequestSenderException | ResolutionException e) {
            throw new RequestSenderActorException(e);
        }
    }
}
