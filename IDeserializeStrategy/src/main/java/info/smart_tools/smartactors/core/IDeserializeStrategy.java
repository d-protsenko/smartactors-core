package info.smart_tools.smartactors.core;


import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Strategy for different deserialization logic of http request to message
 */
public interface IDeserializeStrategy {
    IMessage deserialize(FullHttpRequest request) throws DeserializationException;
}
