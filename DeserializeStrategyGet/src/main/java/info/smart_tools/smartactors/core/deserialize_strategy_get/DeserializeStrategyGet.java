package info.smart_tools.smartactors.core.deserialize_strategy_get;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.exceptions.DeserializationException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by sevenbits on 31.08.16.
 */
public class DeserializeStrategyGet implements IDeserializeStrategy {

    @Override
    public IObject deserialize(FullHttpRequest request) throws DeserializationException {
        try {
            IObject resultIObject = IOC.resolve(Keys.getOrAdd("EmptyIObject"));
            String uri = request.uri();
            IFieldName messageMapIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageMapId");
            if (uri.contains("/")) {
                String messageMapId = uri.split("/")[1];
                if (uri.contains("?")) {
                    messageMapId = messageMapId.substring(0, messageMapId.indexOf('?'));
                }
                resultIObject.setValue(messageMapIdFieldName, messageMapId);
            }
            if (uri.contains("?")) {
                String argsString = uri.substring(uri.indexOf('?') + 1);
                String args[] = argsString.split("&");
                for (String arg : args) {
                    String keyValue[] = arg.split("=");
                    IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), keyValue[0]);
                    resultIObject.setValue(fieldName, keyValue[1]);
                }
            }
            return resultIObject;
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
            throw new DeserializationException(e);
        }
    }

}