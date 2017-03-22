package info.smart_tools.smartactors.message_processing.response_sender_receiver;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;

/**
 * Receiver that sends a response to the message.
 */
public class ResponseSenderReceiver implements IMessageReceiver {
    private final IFieldName responseStrategyFN;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependency
     */
    public ResponseSenderReceiver() throws ResolutionException {
        responseStrategyFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "responseStrategy");
    }

    @Override
    public void receive(final IMessageProcessor processor) throws MessageReceiveException, AsynchronousOperationException {
        try {
            IResponseStrategy responseStrategy = (IResponseStrategy) processor.getContext().getValue(responseStrategyFN);
            responseStrategy.sendResponse(processor.getEnvironment());
        } catch (ReadValueException | InvalidArgumentException | ResponseException | NullPointerException e) {
            throw new MessageReceiveException("Error occurred sending response.", e);
        }
    }
}
