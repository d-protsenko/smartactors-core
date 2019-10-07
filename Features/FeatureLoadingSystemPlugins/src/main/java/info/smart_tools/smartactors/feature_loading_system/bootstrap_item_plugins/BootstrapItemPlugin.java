package info.smart_tools.smartactors.feature_loading_system.bootstrap_item_plugins;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

public class BootstrapItemPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap the bootstrap
     */
    public BootstrapItemPlugin(final IBootstrap bootstrap) {
        super(bootstrap);
    }

    /**
     * Register {@link info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem} creation strategy.
     *
     * @throws ResolutionException if error occurs resolving key or strategy dependencies
     * @throws RegistrationException if error occurs registering the strategy
     */
    @Item("bootstrapItem:creation_strategy")
    public void registerBootstrapItemCreationStrategy()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(
                Keys.getKeyByName("bootstrap item"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> new BootstrapItem((String) args[0])
                )
        );
    }

    /**
     * Unregisters {@link info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem} creation strategy.
     */
    @ItemRevert("bootstrapItem:creation_strategy")
    public void unregisterBootstrapItemCreationStrategy() {
        String[] keyNames = { "bootstrap item" };
        Keys.unregisterByNames(keyNames);
    }
}
