package info.smart_tools.smartactors.core.iresponse_sender;

import info.smart_tools.smartactors.core.iobject.IObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.CompletableFuture;

public interface IResponseSender {
    CompletableFuture<Void> write(final IObject responseObject, final HttpRequest request,
                                  final ChannelHandlerContext ctx);
}
