package info.smart_tools.smartactors.plugin.save_session_actor;

import info.smart_tools.smartactors.actors.save_session.SaveSessionActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

/**
 * Plugin for register SaveSessionPlugin
 */
public class SaveSessionActorPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * defalt constructor
     * @param bootstrap the bootstrap
     */
    public SaveSessionActorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }


    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("SaveSessionActorPlugin");

            item
                    .after("IOC")
                    .before("starter")
                    .process(() -> {
                        try {
                            IKey saveSessionActorKey = Keys.getOrAdd(SaveSessionActor.class.getCanonicalName());
                            IOC.register(saveSessionActorKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> {
                                        try {
                                            return new SaveSessionActor((IObject) args[0]);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                            ));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("SaveSessionActor plugin can't load: can't get SaveSessionActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("SaveSessionActor plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("SaveSessionActor plugin can't load: can't register new strategy", e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
