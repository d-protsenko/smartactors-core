package info.smart_tools.smartactors.core.http_server;

import info.smart_tools.smartactors.core.tcp_server.TcpServer;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * A server which handles HTTP requests.
 * TODO: configuration
 * TODO: handle OPTION requests to allow cross-domain requests
 * TODO: handle input/output compression (e.g. gzip, deflate etc.)
 */
public class HttpServer extends TcpServer {
    public HttpServer(int port, ChannelInboundHandler requestHandler) {
        super(port, requestHandler);
    }

    @Override
    protected ChannelPipeline setupPipeline(ChannelPipeline pipeline) {
        return super.setupPipeline(pipeline).addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(4096)
        );
    }
}
