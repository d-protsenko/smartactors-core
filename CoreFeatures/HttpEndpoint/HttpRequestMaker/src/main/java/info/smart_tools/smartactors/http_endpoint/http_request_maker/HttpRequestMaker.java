package info.smart_tools.smartactors.http_endpoint.http_request_maker;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint.interfaces.imessage_mapper.IMessageMapper;
import info.smart_tools.smartactors.endpoint.irequest_maker.IRequestMaker;
import info.smart_tools.smartactors.endpoint.irequest_maker.exception.RequestMakerException;
import info.smart_tools.smartactors.http_endpoint.message_to_bytes_mapper.MessageToBytesMapper;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.util.List;

/**
 * {@link IRequestMaker} implementation for {@link FullHttpRequest}
 */
public class HttpRequestMaker implements IRequestMaker<FullHttpRequest> {
    private IFieldName uriFieldName;
    private IFieldName methodFieldName;
    private IFieldName headersFieldName;
    private IFieldName nameFieldName;
    private IFieldName valueFieldName;
    private IFieldName cookiesFieldName;
    private IFieldName contentFieldName;

    public HttpRequestMaker() throws ResolutionException {
        uriFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "uri");
        methodFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "method");
        headersFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "headers");
        nameFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "name");
        valueFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "value");
        cookiesFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "cookie");
        contentFieldName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "content");
    }

    @Override
    public FullHttpRequest make(final IObject request) throws RequestMakerException {
        try {
            HttpMethod method = HttpMethod.valueOf((String) request.getValue(methodFieldName));
            URI uri = URI.create((String) request.getValue(uriFieldName));
            FullHttpRequest httpRequest = null;
            if (request.getValue(contentFieldName) == null) {
                httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri.getRawPath());
            } else {
                IMessageMapper<byte[]> messageMapper = IOC.resolve(Keys.getOrAdd(MessageToBytesMapper.class.getCanonicalName()));
                byte[] content = messageMapper.serialize((IObject) request.getValue(contentFieldName));
                httpRequest = new DefaultFullHttpRequest(
                        HttpVersion.HTTP_1_1, method, uri.getRawPath(), Unpooled.copiedBuffer(content)
                );
                httpRequest.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpRequest.content().readableBytes());
                httpRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            }
            httpRequest.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            List<IObject> headers = (List<IObject>) request.getValue(headersFieldName);
            if (null != headers) {
                for (IObject header : headers) {
                    httpRequest.headers().set((String) header.getValue(nameFieldName), header.getValue(valueFieldName));
                }
            }

            List<IObject> cookies = (List<IObject>) request.getValue(cookiesFieldName);
            if (null != cookies) {
                for (IObject cookie : cookies) {
                    httpRequest.headers().set("Cookie", ClientCookieEncoder.encode(
                            (String) cookie.getValue(nameFieldName),
                            (String) cookie.getValue(valueFieldName))
                    );
                }
            }
            return httpRequest;
        } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
            throw new RequestMakerException(e);
        }
    }
}
