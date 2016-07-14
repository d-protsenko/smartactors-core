package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.endpoint_handler.EndpointHandler;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.Map;


/**
 * Endpoint handler for HTTP requests.
 */
public class HttpRequestHandler extends EndpointHandler<ChannelHandlerContext, FullHttpRequest> {
    private final Map<String, IDeserializeStrategy> deserializeStrategies;

    /**
     * Constructor for HttpRequestHandler
     *
     * @param scope scope for HttpRequestHandler
     * @param environmentHandler handler for environment
     * @param receiver chain, that should receive message
     * @param deserializeStrategies map of the deserialize strategies, where key is content-type
     *                              and value is strategy for that content type
     * @throws ResolutionException
     */
    public HttpRequestHandler(
            final IScope scope, final IEnvironmentHandler environmentHandler, final IReceiverChain receiver,
            final Map<String, IDeserializeStrategy> deserializeStrategies
    ) throws ResolutionException {
        super(receiver, environmentHandler, scope);
        this.deserializeStrategies = deserializeStrategies;
    }

    @Override
    protected IObject getEnvironment(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        IObject environment = deserializeStrategies.get(request.headers().get(HttpHeaders.Names.CONTENT_TYPE))
                .deserialize(request);
        IFieldName contextFieldName = IOC.resolve(Keys.getOrAdd(FieldName.class.getCanonicalName()), "context");
        IFieldName requestFieldName = IOC.resolve(Keys.getOrAdd(FieldName.class.getCanonicalName()), "request");
        environment.setValue(contextFieldName, ctx);
        environment.setValue(requestFieldName, request);
        return environment;
    }
}
