package info.smart_tools.smartactors.core.https_server;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.ssl_context_provider.SSLContextProvider;
import info.smart_tools.smartactors.core.ssl_context_provider.exceptions.SSLContextProviderException;
import info.smart_tools.smartactors.core.tcp_server.TcpServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * A server which handles HTTPS requests.
 */
public class HttpsServer extends TcpServer {

    private final SSLContextProvider contextProvider;
    private final int maxContentLength;

    /**
     * Constructor
     *
     * @param port             port of the tcp server
     * @param requestHandler   channel for tcp server
     * @param maxContentLength max length of the content
     * @param contextProvider  ssl context provider
     */
    public HttpsServer(final int port, final ChannelInboundHandler requestHandler, final int maxContentLength,
                       final SSLContextProvider contextProvider) {
        super(port, requestHandler);
        this.contextProvider = contextProvider;
        this.maxContentLength = maxContentLength;
    }

    @Override
    protected ChannelPipeline setupPipeline(final ChannelPipeline pipeline) {
        SSLEngine sslEngine = null;
        try {
            sslEngine = contextProvider.get().createSSLEngine();
        } catch (SSLContextProviderException e) {
        }

        sslEngine.setUseClientMode(false);
        return super.setupPipeline(pipeline).addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(maxContentLength)
        ).addFirst("ssl", new SslHandler(sslEngine));
    }
}