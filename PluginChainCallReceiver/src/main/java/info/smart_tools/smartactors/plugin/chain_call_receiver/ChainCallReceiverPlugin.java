package info.smart_tools.smartactors.plugin.chain_call_receiver;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.chain_call_receiver.ChainCallReceiver;
import info.smart_tools.smartactors.core.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Implementation of {@link IPlugin}.
 * Plugin creates and register into IOC strategy for creation instance of
 * {@link ChainCallReceiver}.
 */
public class ChainCallReceiverPlugin implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public ChainCallReceiverPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load()
            throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ChainCallReceiver");
            item
                    .after("IOC")
                    .before("configure")
                    .after("IFieldPlugin")
                    .after("IFieldNamePlugin")
                    .after("receiver_chains_storage")
                    .after("ChainChoiceStrategy")
                    .process(
                            () -> {
                                try {
                                    IOC.register(
                                            IOC.resolve(
                                                    IOC.getKeyForKeyStorage(),
                                                    ChainCallReceiver.class.getCanonicalName()
                                            ),
                                            new ApplyFunctionToArgumentsStrategy(
                                                    (args) -> {
                                                        try {
                                                            IFieldName fieldName = IOC.resolve(
                                                                    IOC.resolve(
                                                                            IOC.getKeyForKeyStorage(),
                                                                            IFieldName.class.getCanonicalName()
                                                                    ),
                                                                    "strategyDependency"
                                                            );
                                                            IChainChoiceStrategy strategy = IOC.resolve(
                                                                    IOC.resolve(
                                                                            IOC.getKeyForKeyStorage(),
                                                                            ((IObject) args[0]).getValue(fieldName)
                                                                    )
                                                            );
                                                            IChainStorage storage = IOC.resolve(
                                                                    IOC.resolve(
                                                                            IOC.getKeyForKeyStorage(),
                                                                            IChainStorage.class.getCanonicalName()
                                                                    )
                                                            );
                                                            return new ChainCallReceiver(storage, strategy);
                                                        } catch (Exception e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                            )
                                    );
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            "Could not create or register chain call receiver.", e
                                    );
                                }
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ChainCallReceiver plugin'", e);
        }
    }
}
