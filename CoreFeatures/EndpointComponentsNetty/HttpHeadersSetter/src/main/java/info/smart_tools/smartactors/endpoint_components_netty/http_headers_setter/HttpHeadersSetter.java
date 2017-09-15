package info.smart_tools.smartactors.endpoint_components_netty.http_headers_setter;

import info.smart_tools.smartactors.endpoint_interfaces.imessage_byte_array.IOutboundMessageByteArray;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers.IBypassMessageHandler;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import io.netty.handler.codec.http.*;

import java.util.List;

/**
 * Headers setter for {@link FullHttpResponse}
 * This implementation extract headers from context of the environment and set them into response
 * Headers should presents as {@link List<IObject>}
 * <pre>
 *     "headers": [
 *         {
 *             "name": "nameOfTheCookie",
 *             "value": "valueOfTheCookie"
 *         }
 *     ]
 * </pre>
 */
public class HttpHeadersSetter<TMsg extends FullHttpMessage, TCtx>
        implements IBypassMessageHandler<IDefaultMessageContext<IObject, IOutboundMessageByteArray<TMsg>, TCtx>> {
    IField contextField;
    IField headersField;
    IFieldName headerName;
    IFieldName headerValue;

    public HttpHeadersSetter() throws ResolutionException {
        contextField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "context");
        headersField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "headers");
        headerName = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "name");
        headerValue = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "value");
    }

    @Override
    public void handle(
        IMessageHandlerCallback<IDefaultMessageContext<IObject, IOutboundMessageByteArray<TMsg>, TCtx>> next,
        IDefaultMessageContext<IObject, IOutboundMessageByteArray<TMsg>, TCtx> context)
            throws MessageHandlerException {
        try {
            FullHttpMessage httpMessage = context.getDstMessage().getMessage();
            IObject messageContext = contextField.in(context.getSrcMessage());

            httpMessage.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            List<IObject> headers = headersField.in(messageContext, List.class);

            for (IObject header : headers) {
                    httpMessage.headers().set(String.valueOf(header.getValue(headerName)),
                            String.valueOf(header.getValue(headerValue)));
            }
            httpMessage.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpMessage.content().readableBytes());
            httpMessage.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        } catch (Exception e) {
            throw new MessageHandlerException(e);
        }

        next.handle(context);
    }
}
