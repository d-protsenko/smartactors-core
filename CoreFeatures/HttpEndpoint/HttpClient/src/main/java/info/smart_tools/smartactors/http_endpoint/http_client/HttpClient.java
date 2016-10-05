package info.smart_tools.smartactors.http_endpoint.http_client;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.iclient.IClientConfig;
import info.smart_tools.smartactors.endpoint.interfaces.irequest_sender.exception.RequestSenderException;
import info.smart_tools.smartactors.endpoint.interfaces.iresponse_handler.IResponseHandler;
import info.smart_tools.smartactors.http_endpoint.netty_client.NettyClient;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;

import java.net.URI;
import java.util.List;

/**
 * Client for HTTP server
 */
public class HttpClient extends NettyClient<HttpRequest> {
    private IFieldName uriFieldName;
    private IFieldName methodFieldName;
    private IFieldName headersFieldName;
    private IFieldName nameFieldName;
    private IFieldName valueFieldName;
    private IFieldName cookiesFieldName;
    private IFieldName messageMapIdFieldName;
    private String messageMapId;
    private IResponseHandler inboundHandler;

    /**
     * Constructor for http client
     *
     * @param serverUri      URI of the server, that will receive requests
     * @param inboundHandler Channel
     * @throws RequestSenderException if there are exception on resolving IFieldName
     */
    public HttpClient(final URI serverUri, final IResponseHandler inboundHandler) throws RequestSenderException {
        super(serverUri, NioSocketChannel.class, inboundHandler);
        try {
            uriFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uri");
            methodFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "method");
            headersFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "headers");
            nameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
            valueFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "value");
            cookiesFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "cookie");
            messageMapIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageMapId");
        } catch (ResolutionException e) {
            throw new RequestSenderException(e);
        }
        this.inboundHandler = inboundHandler;
    }

    /**
     * Constructor with using client config
     *
     * @param clientConfig Configuration of the current client
     */
    public HttpClient(final IClientConfig clientConfig) {
        super(NioSocketChannel.class, clientConfig);
    }

    //TODO:: set maxContentLength from configuration
    @Override
    protected ChannelPipeline setupPipeline(final ChannelPipeline pipeline) {

        return super.setupPipeline(pipeline)
                .addLast(new HttpClientCodec(), new HttpObjectAggregator(4096))
                .addLast("handleResponse", new SimpleChannelInboundHandler<FullHttpResponse>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
                                inboundHandler.handle(channelHandlerContext, response);
                            }
                        }
                );
    }

    @Override
    public void sendRequest(final IObject request) throws RequestSenderException {
        try {
            HttpMethod method = HttpMethod.valueOf((String) request.getValue(methodFieldName));
            String uri = (String) request.getValue(uriFieldName);
            FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
            List<IObject> headers = (List<IObject>) request.getValue(headersFieldName);
            if (null != headers) {
                for (IObject header : headers) {
                    httpRequest.headers().set((String) header.getValue(nameFieldName), header.getValue(valueFieldName));
                }
            }
            messageMapId = (String) request.getValue(messageMapIdFieldName);
            List<IObject> cookies = (List<IObject>) request.getValue(cookiesFieldName);
            if (null != cookies) {
                for (IObject cookie : cookies) {
                    httpRequest.headers().set("Cookie", ClientCookieEncoder.STRICT.encode(
                            (String) cookie.getValue(nameFieldName),
                            (String) cookie.getValue(valueFieldName))
                    );
                }
            }
            send(httpRequest);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new RequestSenderException(e);
        }
    }
}
