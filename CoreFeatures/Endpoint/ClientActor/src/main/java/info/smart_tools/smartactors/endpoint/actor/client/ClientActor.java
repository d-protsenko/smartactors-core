package info.smart_tools.smartactors.endpoint.actor.client;

import info.smart_tools.smartactors.endpoint.actor.client.exception.RequestSenderActorException;
import info.smart_tools.smartactors.endpoint.actor.client.wrapper.ClientActorMessage;
import info.smart_tools.smartactors.endpoint.interfaces.irequest_sender.IRequestSender;
import info.smart_tools.smartactors.endpoint.interfaces.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

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
        try {
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
            ITask task =
                    () -> {
                        try {
                            IOC.resolve(Keys.getOrAdd("sendRequestHttp"), message.getRequest());
                        } catch (ResolutionException e) {
                            throw new RuntimeException(e);
                        }
                    };
            queue.put(task);
        } catch (ResolutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
