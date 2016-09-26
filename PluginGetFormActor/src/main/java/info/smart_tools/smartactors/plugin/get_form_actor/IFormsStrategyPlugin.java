package info.smart_tools.smartactors.plugin.get_form_actor;

import info.smart_tools.smartactors.actors.get_form.strategy.FirstItemStrategy;
import info.smart_tools.smartactors.actors.get_form.strategy.IFormsStrategy;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

public class IFormsStrategyPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public IFormsStrategyPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("GetFormActorPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                        try {
                            IKey actorKey = Keys.getOrAdd(IFormsStrategy.class.getCanonicalName());
                            IOC.register(actorKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> {
                                        try {
                                            return new FirstItemStrategy();
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                            throw new RuntimeException(e);
                        }
                    });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load GetFormActor plugin", e);
        }
    }
}
