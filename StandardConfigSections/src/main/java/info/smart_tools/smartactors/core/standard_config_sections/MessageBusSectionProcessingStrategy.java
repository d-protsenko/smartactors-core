package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.HttpEndpoint;
import info.smart_tools.smartactors.core.environment_handler.EnvironmentHandler;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.iasync_service.IAsyncService;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.imessage_bus_container.IMessageBusContainer;
import info.smart_tools.smartactors.core.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.core.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.message_bus.MessageBus;
import info.smart_tools.smartactors.core.message_bus_container_with_scope.MessageBusContainer;
import info.smart_tools.smartactors.core.message_bus_handler.MessageBusHandler;
import info.smart_tools.smartactors.core.message_processing.IReceiverChain;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;

import java.util.List;

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
            throw new ConfigurationProcessingException("Error occurred loading \"endpoint\" configuration section.", e);
        } catch (ResolutionException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"endpoint\".", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
