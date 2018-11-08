package info.smart_tools.smartactors.message_processing_plugins.message_processor_and_sequence_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing.message_processing_sequence.MessageProcessingSequence;
import info.smart_tools.smartactors.message_processing.message_processing_sequence.dump_recovery.MessageProcessingSequenceRecoveryStrategy;
import info.smart_tools.smartactors.message_processing.message_processor.FinalTask;
import info.smart_tools.smartactors.message_processing.message_processor.MessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

/**
 *
 */
public class PluginMessageProcessorAndSequence implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     *
     * @param bootstrap    the bootstrap
     */
    public PluginMessageProcessorAndSequence(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "after exception actions" - register after exception action strategy */
            IBootstrapItem<String> afterExceptionActions = new BootstrapItem("after exception actions");

            afterExceptionActions
                    .after("IOC")
                    .process(() -> {
                        try {
                            IAction<IMessageProcessingSequence> breakAction = IMessageProcessingSequence::end;
                            IAction<IMessageProcessingSequence> continueAction = (mps) -> {
                            };
                            IAction<IMessageProcessingSequence> repeatAction = (mps) -> {
                                // ToDo: strange code - probably must replace "currentLevel - 1" by "currentLevel"
                                // ToDo: but then it does nothing
                                int currentLevel = mps.getCurrentLevel();
                                int repeatStep = mps.getStepAtLevel(currentLevel - 1);
                                mps.goTo(currentLevel - 1, repeatStep);
                            };
                            IOC.register(
                                    IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "afterExceptionAction#break"),
                                    new SingletonStrategy(breakAction)
                            );
                            IOC.register(
                                    IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "afterExceptionAction#continue"),
                                    new SingletonStrategy(continueAction)
                            );
                            IOC.register(
                                    IOC.resolve(IOC.getKeyForKeyByNameResolutionStrategy(), "afterExceptionAction#repeat"),
                                    new SingletonStrategy(repeatAction)
                            );
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't get AfterExceptionAction key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "after exception actions";
                        String keyName = "";

                        try {
                            keyName = "afterExceptionAction#break";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "afterExceptionAction#continue";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "afterExceptionAction#repeat";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });
            bootstrap.add(afterExceptionActions);

            /* "message_processor" - register message processor creation strategy */
            IBootstrapItem<String> processorItem = new BootstrapItem("message_processor");

            processorItem
                    .after("IOC")
                    .after("wds_object")
                    .after("IFieldNamePlugin")
                    .after("root_upcounter")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.resolveByName("final task"),
                                    new CreateNewInstanceStrategy(args -> {
                                        try {
                                            return new FinalTask((IObject) args[0]);
                                        } catch (Exception e) {
                                            throw new RuntimeException("Could not create instance of FinalTask.");
                                        }
                                    })
                            );
                            IOC.register(
                                    Keys.resolveByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                                    new CreateNewInstanceStrategy(args -> {
                                        IQueue<ITask> taskQueue = (IQueue<ITask>) args[0];
                                        IMessageProcessingSequence sequence = (IMessageProcessingSequence) args[1];
                                        IObject config;

                                        if (args.length > 2) {
                                            config = (IObject) args[2];
                                        } else {
                                            try {
                                                config = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.iobject.IObject"));
                                            } catch (ResolutionException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }

                                        try {
                                            return new MessageProcessor(taskQueue, sequence, config);
                                        } catch (InvalidArgumentException | ResolutionException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't get MessageProcessorAndSequence key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("MessageProcessorAndSequence plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "message_processor";
                        String keyName = "";

                        try {
                            keyName = "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }

                        try {
                            keyName = "final task";
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(processorItem);

            /* "message_processing_sequence" - register sequence creation strategy */
            IBootstrapItem<String> sequenceItem = new BootstrapItem("message_processing_sequence");

            sequenceItem
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.resolveByName("info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                                    new CreateNewInstanceStrategy(args -> {
                                        int stackDepth = ((Number) args[0]).intValue();
                                        Object mainChainName = args[1];
                                        IObject message = (IObject)args[2];

                                        try {
                                            boolean switchScopeOnStartup = args.length > 3 ? (Boolean)args[3] : true;
                                            return new MessageProcessingSequence(stackDepth, mainChainName, message, switchScopeOnStartup);
                                        } catch (InvalidArgumentException | ResolutionException | ChainNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (ResolutionException | InvalidArgumentException | RegistrationException e) {
                            throw new ActionExecuteException(e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "message_processing_sequence";
                        String keyName = "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence";
                        try {
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(sequenceItem);

            /* "message_processing_sequence_dump_recovery" - register strategy recovering sequence state from dump */
            IBootstrapItem<String> recoverItem = new BootstrapItem("message_processing_sequence_dump_recovery");

            recoverItem
                    .after("IOC")
                    .after("IFieldNamePlugin")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.resolveByName("recover message processing sequence"),
                                    new MessageProcessingSequenceRecoveryStrategy());
                        } catch (ResolutionException | RegistrationException e) {
                            throw new ActionExecuteException(e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "message_processing_sequence_dump_recovery";
                        String keyName = "recover message processing sequence";
                        try {
                            IOC.remove(Keys.resolveByName(keyName));
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        } catch (ResolutionException e) { }
                    });

            bootstrap.add(recoverItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
