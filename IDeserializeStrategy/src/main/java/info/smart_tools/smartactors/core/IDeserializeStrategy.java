package info.smart_tools.smartactors.core;


import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.iobject.IObject;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Strategy for different deserialization logic of http request to message
 */
public interface IDeserializeStrategy {
    IObject deserialize(FullHttpRequest request) throws DeserializationException;
}
