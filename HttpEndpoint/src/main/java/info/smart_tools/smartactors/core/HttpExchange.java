package info.smart_tools.smartactors.core;


import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage.IMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smat_tools.smartactors.core.iexchange.IExchange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.concurrent.CompletableFuture;

/**
 * Exchange object for received HTTP request.
 * It sends a response to the request and closes the connection.
 * <p>
 * To make exchange set cookies (using "Set-Cookie" header) response message should contain "setCookies" field with object like the following:
 * <pre>
 *     {
 *         "$cookie-name$": {
 *             "value": "FEmkDo5UJFo39DKvmDk9", // cookie value
 *             "maxAge": 60 // max cookie storage time in seconds (optional)
 *         },
 *         . . .
 *     }
 * </pre>
 * <p>
 * To make exchange set headers, response message should contain "setHeaders" field with object like the following:
 * <pre>
 *     {
 *         "$header-name$":"$header-value$",
 *         . . .
 *     }
 * </pre>
 */
public class HttpExchange implements IExchange {
    private ChannelHandlerContext ctx;
    @SuppressWarnings("unused")
    private HttpRequest httpRequest;
    private IMessageMapper<byte[]> messageMapper;
    private IMessage requestMessage;

    private static IFieldName cookiesField;
    private static IFieldName headersF;
    private static IFieldName valueCookieField;
    private static IFieldName maxAgeSecondsField;

    public HttpExchange(IMessage requestMessage, ChannelHandlerContext ctx, HttpRequest httpRequest, IMessageMapper<byte[]> messageMapper) throws ResolutionException {
        this.ctx = ctx;
        this.httpRequest = httpRequest;
        this.messageMapper = messageMapper;
        this.requestMessage = requestMessage;
        cookiesField = IOC.resolve(Keys.getOrAdd(FieldName.class.toString()), "cookies");
        headersF = IOC.resolve(Keys.getOrAdd(FieldName.class.toString()), "setHeaders");
        valueCookieField = IOC.resolve(Keys.getOrAdd(FieldName.class.toString()), "value");
        maxAgeSecondsField = IOC.resolve(Keys.getOrAdd(FieldName.class.toString()), "maxAge");
    }

    @Override
    public CompletableFuture<Void> write(IObject responseMessage) {
        ByteBuf serializedMessage = Unpooled.wrappedBuffer(messageMapper.serialize(responseMessage));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, getResponseStatus(responseMessage), serializedMessage);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        //TODO:: Make OPTIONS request handler, which will be set this header
        response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        setHeadersFromMessage(response, requestMessage);
        setCookiesFromMessage(response, requestMessage);
        boolean keepAlive = HttpHeaders.isKeepAlive(httpRequest);
        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        ChannelFuture writeFuture = ctx.writeAndFlush(response);
        if (!keepAlive) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }

        return CompletableNettyFuture.from(writeFuture);
    }

    private HttpResponseStatus getResponseStatus(IObject responseMessage) {
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

    private void setCookiesFromMessage(FullHttpResponse httpResponse, IMessage message) {
        try {
            IObject cookies = (IObject) message.getValue(cookiesField);
            /*IObjectIterator cookiesIterator = cookies.iterator();

            while (cookiesIterator.next()) {
                IObject cookieObject = IOC.resolve(IObject.class, cookiesIterator.getValue());
                Cookie cookie = new DefaultCookie(
                        cookiesIterator.getName().toString(),
                        valueCookieField.from(cookieObject, String.class));

                Integer maxCookieAge = maxAgeSecondsField.from(cookieObject, Integer.class);

                if (maxCookieAge != null) {
                    cookie.setDiscard(false);
                    cookie.setMaxAge(maxCookieAge);
                }

                httpResponse.headers().set(HttpHeaders.Names.SET_COOKIE,
                        ServerCookieEncoder.encode(cookie));
            }*/
        } catch (ReadValueException | NullPointerException e) {
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    private void setHeadersFromMessage(FullHttpResponse httpResponse, IMessage message) {
        try {
            IObject headers = (IObject) message.getValue(headersF);
            /*IObjectIterator headersIterator = headers.iterator();

            while (headersIterator.next()) {
                httpResponse.headers().set(headersIterator.getName().toString(), headersIterator.getValue().toString());
            }*/
        } catch (ReadValueException | NullPointerException e) {
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}
