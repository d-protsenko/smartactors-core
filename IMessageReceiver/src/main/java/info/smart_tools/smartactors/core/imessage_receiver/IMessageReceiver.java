package info.smart_tools.smartactors.core.imessage_receiver;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.imessage_receiver.exception.MessageReceiveException;

/**
 * Interface for a message receiver - object that can receive messages.
 */
public interface IMessageReceiver {
    /**
     * Receive the message and execute given action when done.
     *
     * Passes exception to the action if any error occurs while processing the message.
     *
     * @param message the message
     * @param onEnd the action that should be executed when message processing by this receiver is completed
     * @throws MessageReceiveException if error occurs receiving the message
     */
    void receive(IMessage message, IAction<Throwable> onEnd) throws MessageReceiveException;
}
