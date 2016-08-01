package info.smart_tools.smartactors.plugin.chain_call_receiver_strategy;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

/**
 * Implementation of {@link IPlugin}.
 * Plugin creates and register into IOC {@link IChainChoiceStrategy} strategy.
 */
public class ChainChoiceStrategyPlugin implements IPlugin {


    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public ChainChoiceStrategyPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
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
            IBootstrapItem<String> item = new BootstrapItem("ChainChoiceStrategy");
            item
                    .after("IOC")
                    .before("configure")
                    .after("IFieldPlugin")
                    .after("IFieldNamePlugin")
                    .process(
                            () -> {
                                try {
                                    IChainChoiceStrategy strategy = (a) -> {
                                        try {
                                            return a.getMessage().getValue(
                                                    IOC.resolve(
                                                            IOC.resolve(
                                                                    IOC.getKeyForKeyStorage(),
                                                                    "info.smart_tools.smartactors.core.ifield_name.IFieldName"
                                                            ), "messageMapId"
                                                    )
                                            );
                                        } catch (Exception e) {
                                            throw new RuntimeException("Could not execute chain choice strategy.");
                                        }
                                    };

                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "chain choice strategy"),
                                            new SingletonStrategy(strategy));
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
