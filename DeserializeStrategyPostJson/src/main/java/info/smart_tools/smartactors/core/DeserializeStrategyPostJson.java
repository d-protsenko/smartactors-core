package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.iobject.IObject;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Strategy for deserialization message from post request with application/json content-type.
 * Default strategy.
 */
public class DeserializeStrategyPostJson implements IDeserializeStrategy {
    private final IMessageMapper<byte[]> messageMapper;

    public DeserializeStrategyPostJson(IMessageMapper<byte[]> messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public IObject deserialize(FullHttpRequest request) throws info.smart_tools.smartactors.core.exceptions.DeserializationException {
        byte[] bytes = new byte[request.content().capacity()];

        for (int i = 0, size = request.content().capacity(); i < size; i ++) {
            bytes[i] = request.content().getByte(i);
        }
        return messageMapper.deserialize(bytes);
    }
}
