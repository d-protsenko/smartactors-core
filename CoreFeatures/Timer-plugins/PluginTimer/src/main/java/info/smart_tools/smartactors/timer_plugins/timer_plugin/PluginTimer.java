package info.smart_tools.smartactors.timer_plugins.timer_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimer;
import info.smart_tools.smartactors.timer.timer.SystemTimeImpl;
import info.smart_tools.smartactors.timer.timer.TimerImpl;

import java.util.Timer;

/**
 * Plugin that creates and registers in IOC an instance of {@link ITimer}.
 */
public class PluginTimer implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     *
     * @param bootstrap the bootstrap
     */
    public PluginTimer(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("timer");

            item.process(() -> {
                try {
                    IOC.register(Keys.resolveByName("timer"), new SingletonStrategy(
                            new TimerImpl(new Timer("Smart actors system timer", true))));
                } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                    throw new ActionExecuteException(e);
                }
            })
            .revertProcess(() -> {
                String itemName = "timer";
                String keyName = "timer";

                try {
                    IOC.remove(Keys.resolveByName(keyName));
                } catch(DeletionException e) {
                    System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                } catch (ResolutionException e) { }
            });

            bootstrap.add(item);

            item = new BootstrapItem("time");

            item.process(() -> {
                try {
                    IOC.register(Keys.resolveByName("time"), new SingletonStrategy(new SystemTimeImpl()));
                } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                    throw new ActionExecuteException(e);
                }
            })
            .revertProcess(() -> {
                String itemName = "time";
                String keyName = "time";

                try {
                    IOC.remove(Keys.resolveByName(keyName));
                } catch(DeletionException e) {
                    System.out.println("[WARNING] Deregistration of \""+keyName+"\" has failed while reverting \""+itemName+"\" plugin.");
                } catch (ResolutionException e) { }
            });

            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException(e);
        }
    }
}
