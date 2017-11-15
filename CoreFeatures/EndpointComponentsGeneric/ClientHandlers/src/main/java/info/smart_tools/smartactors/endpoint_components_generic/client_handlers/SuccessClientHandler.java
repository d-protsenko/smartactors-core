package info.smart_tools.smartactors.endpoint_components_generic.client_handlers;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.endpoint_interfaces.iclient_callback.IClientCallback;
import info.smart_tools.smartactors.endpoint_interfaces.iclient_callback.exceptions.ClientCallbackException;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IDefaultMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageContext;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.IMessageHandlerCallback;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.exception.MessageHandlerException;
import info.smart_tools.smartactors.endpoint_interfaces.imessage_handler.helpers.ITerminalMessageHandler;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class SuccessClientHandler<TResp>
        implements ITerminalMessageHandler<IDefaultMessageContext<TResp, IObject, IObject>> {
    private final IFieldName callbackFN;

    public SuccessClientHandler() throws ResolutionException {
        callbackFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "callback");
    }

    @Override
    public void handle(
        final IMessageHandlerCallback<IMessageContext> next,
        final IDefaultMessageContext<TResp, IObject, IObject> context)
            throws MessageHandlerException {
        IObject response = context.getDstMessage();
        IObject request = context.getConnectionContext();

        IClientCallback callback;

        try {
            callback = (IClientCallback) request.getValue(callbackFN);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new MessageHandlerException(e);
        }

        try {
            callback.onSuccess(request, response);
        } catch (ClientCallbackException e) {
            //
        } finally {
            try {
                request.deleteField(callbackFN);
            } catch (DeleteValueException | InvalidArgumentException e) {
                //
            }
        }
    }
}
