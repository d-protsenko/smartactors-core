package info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper;


import com.google.common.base.Charsets;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Implementation of {@link IMessageMapper} which map message to byte array
 */
public class MessageToBytesMapper implements IMessageMapper<byte[]> {

    /**
     * Deserialize bytes array of json string to IObject
     *
     * @param serializedInput Array of bytes, that should be deserialize
     * @return {@link IObject} interpretation of serializedInput
     * @throws ResolutionException
     */
    @Override
    public IObject deserialize(final byte[] serializedInput) throws ResolutionException {
        String string = new String(serializedInput);
        if (serializedInput.length == 0) {
            return IOC.resolve(Keys.getOrAdd("EmptyIObject"));
        }
        return IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), string.trim());
    }

    @Override
    public byte[] serialize(final IObject message) {
        try {
            String serializedMessage = message.serialize();
            return serializedMessage.getBytes(Charsets.UTF_8);
        } catch (SerializeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
