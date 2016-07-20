package info.smart_tools.smartactors.plugin.ioc_simple_container;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ioc_container_simple.Container;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;

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
                        } catch (NoSuchFieldException | IllegalAccessException | InvalidArgumentException e) {
                            throw new ActionExecuteException(e);
                        }
                    });

            bootstrap.add(iocContainerItem);

            /* "ioc" - after this item IOC should be ready to use i.e. strategy for key resolution should be registered */
            IBootstrapItem<String> iocItem = new BootstrapItem("ioc");

            iocItem
                    .after("ioc_container")
                    .process(() -> { });

            bootstrap.add(iocItem);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
