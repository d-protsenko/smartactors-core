package info.smart_tools.smartactors.message_processing_plugins.condition_chain_choice_strategy_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing.chain_call_receiver.IChainChoiceStrategy;

public class PluginConditionChainChoiceStrategy implements IPlugin {
    /** Local storage for instance of {@link IBootstrap}*/
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     * @throws InvalidArgumentException if any errors occurred
     */
    public PluginConditionChainChoiceStrategy(final IBootstrap<IBootstrapItem<String>> bootstrap)
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
            IBootstrapItem<String> item = new BootstrapItem("ConditionChainChoiceStrategy");
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
                                            IFieldName conditionFN = IOC.resolve(
                                                    IOC.resolve(
                                                            IOC.getKeyForKeyStorage(),
                                                            IFieldName.class.getCanonicalName()
                                                    ), "chainCondition"
                                            );

                                            if ((Boolean) a.getMessage().getValue(conditionFN)) {
                                                Object name = a.getSequence().getCurrentReceiverArguments().getValue(IOC.resolve(
                                                        IOC.resolve(
                                                                IOC.getKeyForKeyStorage(),
                                                                IFieldName.class.getCanonicalName()
                                                        ), "trueChain"
                                                ));
                                                return IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), name);
                                            }

                                            Object name = a.getSequence().getCurrentReceiverArguments().getValue(IOC.resolve(
                                                    IOC.resolve(
                                                            IOC.getKeyForKeyStorage(),
                                                            IFieldName.class.getCanonicalName()
                                                    ), "falseChain"
                                            ));
                                            return IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), name);

                                        } catch (Exception e) {
                                            throw new RuntimeException("Could not execute condition chain choice strategy.");
                                        }
                                    };

                                    IOC.register(
                                            IOC.resolve(IOC.getKeyForKeyStorage(), "condition chain choice strategy"),
                                            new SingletonStrategy(strategy));
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            "Could not create or register condition chain choice strategy.", e
                                    );
                                }
                            }
                    );
            this.bootstrap.add(item);
        } catch (Throwable e) {
            throw new PluginException("Could not load 'ConditionChainChoiceStrategy plugin'", e);
        }
    }
}
