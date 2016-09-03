package info.smart_tools.smartactors.core.deserialize_strategy_get;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.IParseTree;
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

import java.util.List;
import java.util.Map;

/**
 * Created by sevenbits on 31.08.16.
 */
public class DeserializeStrategyGet implements IDeserializeStrategy {
    IParseTree tree;

    public DeserializeStrategyGet(final List<String> templates) throws ResolutionException {
        this.tree = IOC.resolve(Keys.getOrAdd(IParseTree.class.getCanonicalName()));
        for (String template : templates) {
            tree.addTemplate(template);
        }
    }

    @Override
    public IObject deserialize(final FullHttpRequest request) throws DeserializationException {
        try {
            if (!request.uri().contains("/")) {
                return IOC.resolve(Keys.getOrAdd("EmptyIObject"));
            }
            QueryStringDecoder decoder = IOC.resolve(Keys.getOrAdd(QueryStringDecoder.class.getCanonicalName()),
                    request.uri().substring(request.uri().indexOf("/")));
            IObject resultIObject = IOC.resolve(Keys.getOrAdd("EmptyIObject"));
            decoder.parameters().forEach(
                    (k, v) -> {
                        try {
                            IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), k);
                            resultIObject.setValue(fieldName, v);
                        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            Map<String, String> parameters = tree.match(decoder.uri());
            parameters.forEach(
                    (k, v) -> {
                        try {
                            IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), k);
                            resultIObject.setValue(fieldName, v);
                        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            return resultIObject;
        } catch (ResolutionException e) {
            throw new DeserializationException(e);
        }
    }

}