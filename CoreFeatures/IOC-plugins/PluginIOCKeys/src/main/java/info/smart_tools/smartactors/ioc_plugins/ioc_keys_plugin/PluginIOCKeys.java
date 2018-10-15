package info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;

/**
 *
 */
public class PluginIOCKeys implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginIOCKeys(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> iocKeysItem = new BootstrapItem("ioc_keys");

            iocKeysItem
                    .after("ioc_container")
                    .before("IOC")
                    .process(() -> {
                        try {
                            IOC.register(IOC.getKeyForKeyByNameResolveStrategy(), new ResolveByNameIocStrategy());
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("IOCKeys plugin can't load: can't register new strategy", e);
                        }
                    })
                    .revertProcess(() -> {
                        String itemName = "ioc_keys";
                        String keyName = "";

                        try {
                            keyName = "IOC key for key storage";
                            IOC.remove(IOC.getKeyForKeyByNameResolveStrategy());
                        } catch(DeletionException e) {
                            System.out.println("[WARNING] Deregitration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                        }
                    });

            bootstrap.add(iocKeysItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
