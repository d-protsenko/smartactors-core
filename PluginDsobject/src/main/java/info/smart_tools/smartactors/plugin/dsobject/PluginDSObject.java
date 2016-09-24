package info.smart_tools.smartactors.plugin.dsobject;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 *
 */
public class PluginDSObject implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     * @param bootstrap    the bootstrap
     */
    public PluginDSObject(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> dsObjectItem = new BootstrapItem("iobject");

            dsObjectItem
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(Keys.getOrAdd(IObject.class.getCanonicalName()),
                                    new CreateNewInstanceStrategy(args -> {
                                        if (args.length == 0) {
                                            return new DSObject();
                                        } else if (args.length == 1 && args[0] instanceof String) {
                                            try {
                                                return new DSObject((String) args[0]);
                                            } catch (InvalidArgumentException e) {
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            throw new RuntimeException("Invalid arguments for IObject creation.");
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("Dsobject plugin can't load: can't get Dsobject key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("Dsobject plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("Dsobject plugin can't load: can't register new strategy", e);
                        }
                    });

            bootstrap.add(dsObjectItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
