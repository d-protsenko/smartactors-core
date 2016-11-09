package info.smart_tools.smartactors.http_endpoint.response_content_chunked_json_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse.IResponse;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_content_strategy.IResponseContentStrategy;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.nio.charset.Charset;

/**
 * Class for setting chunked json content of the response
 */
public class ResponseContentChunkedJsonStrategy implements IResponseContentStrategy {
    @Override
    public void setContent(final IObject responseObject, final IResponse response) throws SerializeException {
        response.setContent(serializeIObjectToJson(responseObject));
    }

    private byte[] serializeIObjectToJson(final IObject responseObject) throws SerializeException {
        IFieldName chunkedFieldName = null;
        try {
            chunkedFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "chunked");
            String chunked = (String) responseObject.getValue(chunkedFieldName);
            responseObject.deleteField(chunkedFieldName);
            String responseString = ((String) responseObject.serialize());
            if (!chunked.equals("start")) {
                responseString = responseString.substring(1, responseString.length());
            }
            if (!chunked.equals("end")) {
                responseString = responseString.substring(0, responseString.lastIndexOf('}'));
            }
            responseString = String.valueOf(Integer.toHexString(responseString.length())).concat("\r\n").concat(responseString).concat("\r\n");
            responseObject.setValue(chunkedFieldName, chunked);
            return responseString.getBytes(Charset.forName("UTF-8"));
        } catch (ResolutionException e) {
            throw new SerializeException("Failed to resolve \"IFieldName\"", e);
        } catch (ReadValueException | InvalidArgumentException | DeleteValueException | ChangeValueException e) {
            throw new SerializeException(e);
        }
    }
}