package info.smart_tools.smartactors.message_bus.message_bus_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.exceptions.ResponseException;

/**
 *
 */
public class MessageBusResponseStrategy implements IResponseStrategy {
    private final IFieldName contextFieldName;
    private final IFieldName responseFieldName;
    private final IFieldName replyChainFieldName;
    private final IFieldName responseStrategyFieldName;
    private final IResponseStrategy nullResponseStrategy;

    public MessageBusResponseStrategy()
            throws ResolutionException {
        contextFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "context");
        responseFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "response");
        replyChainFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "messageBusReplyTo");
        responseStrategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "responseStrategy");

        nullResponseStrategy = IOC.resolve(Keys.getOrAdd("null response strategy"));
    }

    @Override
    public void sendResponse(IObject environment) throws ResponseException {
        try {
            IObject context = (IObject) environment.getValue(contextFieldName);
            IObject response = (IObject) environment.getValue(responseFieldName);
            Object replyChainId = context.getValue(replyChainFieldName);

            String sResponse = response.serialize();
            response = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), sResponse);

            MessageBus.send(response, replyChainId);

            context.setValue(responseStrategyFieldName, nullResponseStrategy);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException | SendingMessageException | SerializeException
                | ResolutionException e) {
            throw new ResponseException(e);
        }
    }
}
