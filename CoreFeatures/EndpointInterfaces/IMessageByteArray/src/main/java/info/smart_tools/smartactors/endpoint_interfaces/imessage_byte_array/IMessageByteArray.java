package info.smart_tools.smartactors.endpoint_interfaces.imessage_byte_array;

import java.nio.ByteBuffer;

/**
 * Interface for a object that wraps a message containing a body that is a sequence of bytes.
 *
 * @param <TMessage> type of the wrapped message
 */
public interface IMessageByteArray<TMessage> {
    /**
     * @return original message
     */
    TMessage getMessage();

    /**
     * @return message body represented as a byte buffer
     */
    ByteBuffer getAsByteBuffer();
}
