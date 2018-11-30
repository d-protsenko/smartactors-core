package info.smart_tools.smartactors.ioc_plugins.ioc_simple_container_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.ioc_container_simple.Container;

import java.lang.reflect.Field;

/**
 *
 */
public class PluginIOCSimpleContainer implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public PluginIOCSimpleContainer(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            /* "ioc_container" - set container in IOC service locator */
            IBootstrapItem<String> iocContainerItem = new BootstrapItem("ioc_container");

            iocContainerItem
                    .process(() -> {
                        try {
                            Field field = IOC.class.getDeclaredField("container");
                            field.setAccessible(true);
                            field.set(null, new Container());
                            field.setAccessible(false);
                        } catch (IllegalAccessException e) {
                            throw new ActionExecuteException("IOCSimpleContainer plugin can't load: access to field denied", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("IOCSimpleContainer plugin can't load: can't create strategy", e);
                        } catch (NoSuchFieldException e) {
                            throw new ActionExecuteException("IOCSimpleContainer plugin can't load: field with name 'container' is not found", e);
                        }
                    });

            bootstrap.add(iocContainerItem);

            /* "IOC" - after this item IOC should be ready to use i.e. strategy for key resolution should be registered */
            IBootstrapItem<String> iocItem = new BootstrapItem("IOC");

            iocItem
                    .after("ioc_container")
                    .process(() -> { });

            bootstrap.add(iocItem);
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }
}
