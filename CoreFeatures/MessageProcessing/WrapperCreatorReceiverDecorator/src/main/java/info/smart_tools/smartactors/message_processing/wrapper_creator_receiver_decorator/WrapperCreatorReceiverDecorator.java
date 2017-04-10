package info.smart_tools.smartactors.message_processing.wrapper_creator_receiver_decorator;

import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;

import java.util.Map;

public class WrapperCreatorReceiverDecorator implements IMessageReceiver {
    private final IFieldName wrapperFieldName;
    private final Map<IObject, Object> wrapperConfigurations;

    public WrapperCreatorReceiverDecorator()
            throws ResolutionException {
        wrapperFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "wrapper");
    }

    @Override
    public void receive(IMessageProcessor processor)
            throws MessageReceiveException, AsynchronousOperationException {

    }
}
