package info.smart_tools.smartactors.http_endpoint.deserialize_strategy_post_form_urlencoded;

import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.IDeserializeStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ideserialize_strategy.exceptions.DeserializationException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Strategy for deserialization message from post request with application/x-www-form-urlencoded content-type.
 * Default strategy.
 */
public class DeserializeStrategyPostFormUrlencoded implements IDeserializeStrategy<FullHttpRequest> {

    /**
     * Method, that deserialize application/x-www-form-urlencoded content of request
     * @param request Http request, that should be deserialize
     * @return {@link IObject} deserializated json
     * @throws DeserializationException
     */
    @Override
    public IObject deserialize(final FullHttpRequest request) throws DeserializationException {
        try {
            byte[] bytes = new byte[request.content().capacity()];

            for (int i = 0, size = request.content().capacity(); i < size; i++) {
                bytes[i] = request.content().getByte(i);
            }
            String string = new String(bytes);
            string = string.replaceAll("=", "\":\"");
            string = string.replaceAll("&", "\",\"");
            return IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), "{\"" + string + "\"}");
        } catch (ResolutionException e) {
            throw new DeserializationException("Failed to deserialize request", e);
        }
    }
}
