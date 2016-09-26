package info.smart_tools.smartactors.core.http_response_deserialization_strategy;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * Created by sevenbits on 26.09.16.
 */
public class HttpResponseDeserializationStrategy implements IDeserializeStrategy<FullHttpResponse> {
    private final IMessageMapper<byte[]> messageMapper;

    public HttpResponseDeserializationStrategy(final IMessageMapper<byte[]> messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public IObject deserialize(FullHttpResponse response) throws DeserializationException {
        byte[] bytes = new byte[response.content().capacity()];

        for (int i = 0, size = response.content().capacity(); i < size; i++) {
            bytes[i] = response.content().getByte(i);
        }
        try {
            return messageMapper.deserialize(bytes);
        } catch (ResolutionException e) {
            throw new DeserializationException("Failed to deserialize request", e);
        }
    }
}
