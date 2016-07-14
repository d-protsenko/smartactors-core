package info.smart_tools.smartactors.plugin.create_user;

import info.smart_tools.smartactors.actors.create_user.CreateUserActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Plugin for CreateUserActor
 */
public class CreateUserPlugin implements IPlugin {
    /** Local storage for instance of {@link IBootstrap}*/
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor for CreateUserPlugin
     * @param bootstrap bootstrap
     */
    public CreateUserPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateCreateUserActorPlugin");

            item.process(() -> {
                try {
                    IKey createCreateUserKey = Keys.getOrAdd(CreateUserActor.class.toString());
                    try {
                        IOC.register(createCreateUserKey, new CreateNewInstanceStrategy(
                                (args) -> {
                                    try {
                                        return new CreateUserActor(() -> {
                                            return (String) args[0];
                                        });
                                    } catch (InvalidArgumentException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        ));
                    } catch (RegistrationException | InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ResolutionException e) {
                    throw new RuntimeException(e);
                }
            });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }
}
