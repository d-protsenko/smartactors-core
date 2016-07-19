package info.smart_tools.smartactors.core.receiver_generator;

import info.smart_tools.smartactors.core.receiver_generator.CustomActor;
import info.smart_tools.smartactors.core.receiver_generator.ICustomWrapper;
import info.smart_tools.smartactors.core.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.core.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.core.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.core.message_processing.exceptions.MessageReceiveException;

public class CustomActor_doSomeWork_receiver implements IMessageReceiver {
    private CustomActor usersObject;

    public CustomActor_doSomeWork_receiver(CustomActor object)  {
        this.usersObject = object;

    }

    public void receive(IMessageProcessor processor) throws MessageReceiveException, AsynchronousOperationException  {
        try {
            this.usersObject.doSomeWork((ICustomWrapper) processor.getEnvironment());
        } catch (Throwable e) {
            throw new MessageReceiveException("Could not execute receiver operation.", e);
        }

    }

}