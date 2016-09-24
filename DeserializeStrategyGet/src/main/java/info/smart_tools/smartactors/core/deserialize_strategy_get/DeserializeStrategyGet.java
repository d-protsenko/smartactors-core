package info.smart_tools.smartactors.core.deserialize_strategy_get;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.IParseTree;
import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.ParseTree;
import info.smart_tools.smartactors.core.i_add_request_parameters_to_iobject.IAddRequestParametersToIObject;
import info.smart_tools.smartactors.core.i_add_request_parameters_to_iobject.exception.AddRequestParametersToIObjectException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IDeserializeStrategy} for getting parameters from uri
 */
public class DeserializeStrategyGet implements IAddRequestParametersToIObject {
    private final IParseTree tree;

    /**
     * @param templates templates for accessed uri`s
     * @throws ResolutionException if there are IOC problems
     */
    public DeserializeStrategyGet(final List<String> templates) throws ResolutionException {
        this.tree = IOC.resolve(Keys.getOrAdd(IParseTree.class.getCanonicalName()));
        if (null == templates) {
            return;
        }
        for (String template : templates) {
            tree.addTemplate(template);
        }
    }

    @Override
    public void extract(final IObject message, final Object requestObject) throws AddRequestParametersToIObjectException {
        FullHttpRequest request = (FullHttpRequest) requestObject;
        if (!request.uri().contains("/")) {
            return;
        }
        QueryStringDecoder decoder = new QueryStringDecoder(
                request.uri().substring(request.uri().indexOf("/")));
        decoder.parameters().forEach(
                (k, v) -> {
                    try {
                        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), k);
                        message.setValue(fieldName, v);
                    } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        Map<String, String> parameters = tree.match(decoder.path());
        parameters.forEach(
                (k, v) -> {
                    try {
                        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), k);
                        message.setValue(fieldName, v);
                    } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        return;
    }

}