package info.smart_tools.smartactors.message_processing_plugins.chain_choice_strategy_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;

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
                    .before("starter")
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
                                                                    IFieldName.class.getCanonicalName()
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
                                            "Could not create or register chain choice strategy.", e
                                    );
                                }
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ChainChoiceStrategy plugin'", e);
        }
    }
}
