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
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sevenbits on 31.08.16.
 */
public class DeserializeStrategyGet implements IDeserializeStrategy {

    IFieldName messageMapIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageMapId");
    IFieldName argsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "args");
    List<List<String>> templates = new ArrayList<>();

    public DeserializeStrategyGet(List<String> templates) throws ResolutionException {
        for (String template : templates) {
            this.templates.add(Arrays.asList(template.split("/")));
        }
    }

    @Override
    public IObject deserialize(FullHttpRequest request) throws DeserializationException {
        try {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            IObject resultIObject = null;//IOC.resolve(Keys.getOrAdd(DSObject.));
            //add to resultIObject
            decoder.parameters();
            String[] uriElems = decoder.path().split("/");
            int score[] = new int[templates.size()];
            for (int i = 0; i < templates.size(); i++) {
                List<String> template = templates.get(i);
                for (String templateElem : template) {
                    if (templateElem.startsWith(":")) {
                        score[i] += 1;
                        continue;
                    }
                    if (templateElem.equals(uriElems[i])) {
                        score[i] += 2;
                    }
                }
            }
            String uri = request.uri();
            String[] parts = uri.split("\\?");

            List<String> restApi = Arrays.asList(parts[0].split("/"));
            if (parts[0].contains("/")) {
                String messageMapId = restApi.get(1);
                resultIObject.setValue(messageMapIdFieldName, messageMapId);
            }
            if (parts.length > 1) {
                String args[] = parts[1].split("&");
                for (String arg : args) {
                    String keyValue[] = arg.split("=");
                    IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), keyValue[0]);
                    resultIObject.setValue(fieldName, keyValue[1]);
                }
            }
            resultIObject.setValue(argsFieldName, Arrays.copyOfRange(restApi.toArray(), 2, restApi.size()));
            return resultIObject;
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
            throw new DeserializationException(e);
        }
    }

}