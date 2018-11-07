package info.smart_tools.smartactors.message_processing_plugins.condition_chain_choice_strategy_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.message_processing.condition_chain_choice_strategy.ConditionChainChoiceStrategy;

/**
 * Plugin that registers condition chain choice strategy.
 */
public class PluginConditionChainChoiceStrategy extends BootstrapPlugin {

    /**
     * Constructor
     * @param bootstrap the bootstrap item
     */
    public PluginConditionChainChoiceStrategy(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register condition chain choice strategy as a singleton.
     *
     * @throws ResolutionException if error occurs resoling key or dependencies of chain choice strategy
     * @throws RegistrationException if error occurs registering the strategy
     * @throws InvalidArgumentException if {@link SingletonStrategy} doesn't like our arguments
     */
    @BootstrapPlugin.Item("condition_chain_choice_strategy")
    @BootstrapPlugin.After({"IOC", "IFieldNamePlugin"})
    @BootstrapPlugin.Before({"ChainCallReceiver"})
    public void item()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.resolveByName("condition chain choice strategy"), new SingletonStrategy(new ConditionChainChoiceStrategy()));
    }

    /**
     * Unregisters condition chain choice strategy.
     */
    @BootstrapPlugin.Item("condition_chain_choice_strategy")
    public void revertItem() {
        String itemName = "condition_chain_choice_strategy";
        String keyName = "condition chain choice strategy";
        try {
            IOC.remove(Keys.resolveByName(keyName));
        } catch(DeletionException e) {
            System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
        } catch (ResolutionException e) { }
    }
}
