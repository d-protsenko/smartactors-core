package info.smart_tools.smartactors.iobject_plugins.iobject_simple_impl_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.iobject_simple_implementation.IObjectImpl;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

/**
 *
 */
public class IObjectSimpleImplPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap element
     */
    public IObjectSimpleImplPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }


    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("IObjectSimpleImplPlugin");
            item
                .after("IOC")
                .process(() -> {
                    try {
                        IKey fieldKey = Keys.getOrAdd(IObjectImpl.class.getCanonicalName());
                        IOC.register(fieldKey, new CreateNewInstanceStrategy(
                                (args) -> new IObjectImpl()
                        ));
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("IObjectSimpleImpl plugin can't load: can't get IObjectSimpleImpl key", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("IObjectSimpleImpl plugin can't load: can't create strategy", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("IObjectSimpleImpl plugin can't load: can't register new strategy", e);
                    }
                })
                .revertProcess(() -> {
                    String itemName = "IObjectSimpleImplPlugin";
                    String keyName = "";

                    try {
                        keyName = IObjectImpl.class.getCanonicalName();
                        IOC.remove(Keys.getOrAdd(keyName));
                    } catch(DeletionException e) {
                        System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                    } catch (ResolutionException e) { }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load IObject plugin", e);
        }
    }
}
