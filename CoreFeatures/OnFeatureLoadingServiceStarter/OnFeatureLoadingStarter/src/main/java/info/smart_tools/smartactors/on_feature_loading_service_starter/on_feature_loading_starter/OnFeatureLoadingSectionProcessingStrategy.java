package info.smart_tools.smartactors.on_feature_loading_service_starter.on_feature_loading_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageProcessorProcessException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;

import java.util.List;

/**
 * Initializes OnFeatureLoading using configuration.
 * <p>
 * Expects the following configuration format:
 * <p>
 * <pre>
 *     {
 *         "onFeatureLoading": [
 *                 {
 *                     "chain": "chain_name_1",
 *                     "revert": false,
 *                     "messages": [
 *                         {
 *                             "fieldName1": "value1",
 *                             "fieldName2": "value2",
 *                             ...
 *                         }, {
 *                             ...
 *                         }
 *                     ]
 *                 },
 *                 {
 *                     "chain": "chain_name_1",
 *                 }
 *                 // . . .
 *             ]
 *     }
 * </pre>
 */
public class OnFeatureLoadingSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName sectionNameFieldName;
    private final IFieldName chainFieldName;
    private final IFieldName messagesFieldName;
    private final IFieldName revertFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public OnFeatureLoadingSectionProcessingStrategy()
            throws ResolutionException {
        this.sectionNameFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                "onFeatureLoading"
        );
        this.chainFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                "chain"
        );
        this.revertFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                "revert"
        );
        this.messagesFieldName = IOC.resolve(
                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"),
                "messages"
        );
    }

    @Override
    public void onLoadConfig(final IObject config)
            throws ConfigurationProcessingException {
        try {
            List<IObject> onFeatureLoadingConfig = (List<IObject>) config.getValue(this.sectionNameFieldName);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));

            Integer stackDepth;
            try {
                stackDepth = IOC.resolve(Keys.getOrAdd("default_stack_depth"));
            } catch (ResolutionException e) {
                stackDepth = 5;
            }

            for (IObject task : onFeatureLoadingConfig) {
                Boolean isRevert = (Boolean) task.getValue(this.revertFieldName);
                if ( !isRevert ) {
                    String chainName = (String) task.getValue(this.chainFieldName);
                    Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);
                    ModuleManager.setCurrentMessage(null);
                    IReceiverChain chain = chainStorage.resolve(mapId);
                    List<IObject> messages = (List<IObject>)task.getValue(this.messagesFieldName);
                    for (IObject message : messages) {
                        IMessageProcessingSequence processingSequence = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                                stackDepth,
                                chain
                        );
                        IMessageProcessor messageProcessor = IOC.resolve(
                                IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                                queue,
                                processingSequence
                        );
                        messageProcessor.process(message, (IObject) IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject")));
                    }
                }
            }
        } catch (ReadValueException | InvalidArgumentException | ResolutionException | MessageProcessorProcessException e) {
            throw new ConfigurationProcessingException("Error occurred executing \"onFeatureLoading\" configuration section.", e);
        } catch (ChainNotFoundException e) {
            throw new ConfigurationProcessingException("Error occurred resolving \"chain\".", e);
        }
    }

    @Override
    public void onRevertConfig(final IObject config)
            throws ConfigurationProcessingException {
        ConfigurationProcessingException exception = new ConfigurationProcessingException("Error occurred reverting \"onFeatureLoading\" configuration section.");
        try {
            List<IObject> onFeatureLoadingConfig = (List<IObject>) config.getValue(this.sectionNameFieldName);
            IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(),
                    IChainStorage.class.getCanonicalName()));
            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd("task_queue"));

            Integer stackDepth;
            try {
                stackDepth = IOC.resolve(Keys.getOrAdd("default_stack_depth"));
            } catch (ResolutionException e) {
                stackDepth = 5;
            }

            for (IObject task : onFeatureLoadingConfig) {
                try {
                    Boolean isRevert = (Boolean) task.getValue(this.revertFieldName);
                    if (isRevert) {
                        String chainName = (String) task.getValue(this.chainFieldName);
                        Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);
                        ModuleManager.setCurrentMessage(null);
                        IReceiverChain chain = chainStorage.resolve(mapId);
                        List<IObject> messages = (List<IObject>) task.getValue(this.messagesFieldName);
                        for (IObject message : messages) {
                            try {
                                IMessageProcessingSequence processingSequence = IOC.resolve(
                                        IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                                        stackDepth,
                                        chain
                                );
                                IMessageProcessor messageProcessor = IOC.resolve(
                                        IOC.resolve(IOC.getKeyForKeyByNameResolveStrategy(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                                        queue,
                                        processingSequence
                                );
                                messageProcessor.process(message, (IObject) IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject")));
                            } catch (InvalidArgumentException | ResolutionException | MessageProcessorProcessException | RuntimeException e) {
                                exception.addSuppressed(e);
                            }
                        }
                    }
                } catch (ReadValueException | InvalidArgumentException | ResolutionException e) {
                    exception.addSuppressed(e);
                }
            }
        } catch (ChainNotFoundException | ReadValueException | InvalidArgumentException | ResolutionException e) {
            exception.addSuppressed(e);
        }
        if (exception.getSuppressed().length > 0) {
            throw exception;
        }
    }

    @Override
    public IFieldName getSectionName() {
        return this.sectionNameFieldName;
    }
}
