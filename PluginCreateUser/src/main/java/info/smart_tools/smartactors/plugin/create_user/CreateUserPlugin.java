package info.smart_tools.smartactors.plugin.create_user;

import info.smart_tools.smartactors.actors.create_user.CreateUserActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin.
 * Implements {@link IPlugin}
 * CreateUserActor.
 */
public class CreateUserPlugin implements IPlugin {
    /** Local storage for instance of {@link IBootstrap}*/
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor for CreateUserPlugin
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public CreateUserPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should be not null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateUserActorPlugin");

            item
                .after("IOC")
                .before("starter")
                .process(() -> {
                    try {
                        IKey createCreateUserKey = Keys.getOrAdd(CreateUserActor.class.getCanonicalName());
                        try {
                            IOC.register(createCreateUserKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> {
                                        try {
                                            return new CreateUserActor((IObject) args[0]);
                                        } catch (InvalidArgumentException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            ));
                        } catch (RegistrationException e) {
                            throw new RuntimeException(e);
                        } catch (InvalidArgumentException e) {
                            throw new RuntimeException("Can't create actor with this args: ", e);
                        }
                    } catch (ResolutionException e) {
                        throw new RuntimeException("Can't get ActorParams wrapper or Key for ActorParams", e);
                    }
                });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }
}
