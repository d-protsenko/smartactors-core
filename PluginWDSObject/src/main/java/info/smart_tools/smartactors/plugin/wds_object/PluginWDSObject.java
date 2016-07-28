package info.smart_tools.smartactors.plugin.wds_object;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.wds_object.WDSObject;

/**
 *
 */
public class PluginWDSObject implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor
     *
     * @param bootstrap    the bootstrap
     */
    public PluginWDSObject(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> wdsObjectItem = new BootstrapItem("wds_object");

            wdsObjectItem
                    .after("IOC")
                    .process(() -> {
                        try {
                            IOC.register(
                                    Keys.getOrAdd(WDSObject.class.getCanonicalName()),
                                    new CreateNewInstanceStrategy(args -> {
                                        IObject config = (IObject) args[0];

                                        try {
                                            return new WDSObject(config);
                                        } catch (InvalidArgumentException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (InvalidArgumentException | ResolutionException | RegistrationException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(wdsObjectItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
