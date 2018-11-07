package info.smart_tools.smartactors.message_processing.response_sender_receiver;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;

/**
 * Receiver that sends a response to the message.
 */
public class ResponseSenderReceiver implements IMessageReceiver {
    private final IAction<IObject> action;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependency
     */
    public ResponseSenderReceiver() throws ResolutionException {
        action = IOC.resolve(Keys.getOrAdd("send response action"));
    }

    @Override
    public void receive(final IMessageProcessor processor) throws MessageReceiveException, AsynchronousOperationException {
        try {
            action.execute(processor.getEnvironment());
        } catch (ActionExecuteException | InvalidArgumentException e) {
            throw new MessageReceiveException("Error occurred sending response.", e);
        }
    }

    @Override
    public void dispose() {
        // do nothing
    }
}
