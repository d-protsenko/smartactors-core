package info.smart_tools.smartactors.core.imessage_receiver;

import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.imessage_receiver.exception.MessageReceiveException;

/**
 * Interface for a message receiver - object that can receive messages.
 */
public interface IMessageReceiver {
    /**
     * Receive the message.
     *
     * @param message the message
     * @throws MessageReceiveException if error occurs receiving the message
     */
    void receive(IMessage message) throws MessageReceiveException;
}
