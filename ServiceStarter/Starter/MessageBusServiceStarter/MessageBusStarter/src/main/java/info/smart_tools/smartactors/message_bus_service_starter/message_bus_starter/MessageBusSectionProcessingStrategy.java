package info.smart_tools.smartactors.messagebus_service_starter.messagebus_starter;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.scope.iscope.exception.ScopeException;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_bus.message_bus_handler.MessageBusHandler;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;

/**
 * Initializes MessageBus using configuration.
 * <p>
 * Expects the following configuration format:
 * <p>
 * <pre>
 *     {
 *         "messageBus": {
 *                 "routingChain": "mainChain",
 *                 "stackDepth": 5 (temporarily)
 *                 // . . .
 *             }
 *     }
 * </pre>
 */
public class MessageBusSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;
    private final IFieldName startChainNameFieldName;
    private final IFieldName stackDepthFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public MessageBusSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                "messageBus"
        );
        this.startChainNameFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                "routingChain"
        );
        this.stackDepthFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                "stackDepth"
        );
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            IObject messageBusObject = (IObject) config.getValue(name);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));
            Integer stackDepth = Integer.valueOf(String.valueOf(messageBusObject.getValue(stackDepthFieldName)));
            String startChainName = (String) messageBusObject.getValue(startChainNameFieldName);
            Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), startChainName);
            IReceiverChain chain = chainStorage.resolve(mapId);
            IAction<IObject> finalAction = new IAction<IObject>() {
                @Override
                public void execute(final IObject environment) throws ActionExecuteException, InvalidArgumentException {
                    try {
                        IFieldName messageFieldName = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                                "message"
                        );
                        IFieldName contextFieldName = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                                "context"
                        );
                        IFieldName replyToFieldName = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.getCanonicalName()),
                                "messageBusReplyTo"
                        );
                        IObject context = (IObject) environment.getValue(contextFieldName);
                        MessageBus.send((IObject) environment.getValue(messageFieldName), context.getValue(replyToFieldName));
                    } catch (ResolutionException | ReadValueException | SendingMessageException e) {
                        throw new ActionExecuteException(e);
                    }
                }
            };
            IMessageBusHandler handler = new MessageBusHandler(queue, stackDepth, chain, finalAction);
            ScopeProvider.getCurrentScope().setValue(MessageBus.getMessageBusKey(), handler);
        } catch (ReadValueException | InvalidArgumentException | ScopeProviderException | ScopeException e) {
            throw new ConfigurationProcessingException("Error occurred loading \"client\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"client\".", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
