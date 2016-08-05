package info.smart_tools.smartactors.plugin.sample_actor;

import info.smart_tools.smartactors.actors.SampleActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin for register {@link SampleActor} creation strategy
 */
public class SampleActorPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public SampleActorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    /**
     * Load the plugin for {@link SampleActor}
     * @throws PluginException Throw when plugin can't be load
     */
    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("SampleActorActorPlugin");

            item
                    .after("IOC")
                    .process(() -> {
                try {
                    IOC.register(Keys.getOrAdd("SampleActor"), new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                try {
                                    return new SampleActor();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }));
                } catch (ResolutionException | RegistrationException | InvalidArgumentException e) {
                    throw new ActionExecuteException(e);
                }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load AuthenticationActor plugin", e);
        }
    }
}
