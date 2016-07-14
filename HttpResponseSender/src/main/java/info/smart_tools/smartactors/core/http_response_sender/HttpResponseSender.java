package info.smart_tools.smartactors.core.http_response_sender;


import info.smart_tools.smartactors.core.CompletableNettyFuture;
import info.smart_tools.smartactors.core.IMessageMapper;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Exchange object for received HTTP request.
 * It sends a response to the request and closes the connection.
 */
public class HttpResponseSender implements IResponseSender {
    private final IMessageMapper<byte[]> messageMapper;
    private final String contentType;

    public HttpResponseSender(
            final String contentType,
            final IMessageMapper<byte[]> messageMapper)
            throws ResolutionException {
        this.messageMapper = messageMapper;
        this.contentType = contentType;
    }

    @Override
    public CompletableFuture<Void> write(final IObject responseObject, final HttpRequest request,
                                         final ChannelHandlerContext ctx) {
        ByteBuf serializedMessage = Unpooled.wrappedBuffer(messageMapper.serialize(responseObject));
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, getResponseStatus(responseObject), serializedMessage);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        //TODO:: Make OPTIONS request handler, which will be set this header
        response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

        ChannelFuture writeFuture = ctx.writeAndFlush(response);

        return CompletableNettyFuture.from(writeFuture);
    }

    private HttpResponseStatus getResponseStatus(final IObject responseMessage) {
        try {
            IFieldName exceptionFieldName = IOC.resolve(Keys.getOrAdd(FieldName.class.toString()), "exception");
            Exception ex = (Exception) responseMessage.getValue(exceptionFieldName);
            if (ex != null) {
                if (ex instanceof RuntimeException || ex.getCause() != null && ex.getCause() instanceof RuntimeException) {
                    return HttpResponseStatus.INTERNAL_SERVER_ERROR;
                }
            }
        } catch (ReadValueException ignored) {
        } catch (ResolutionException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }

        return HttpResponseStatus.OK;
    }
}
