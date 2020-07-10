package info.smart_tools.smartactors.plugin.validation_form_actor;

import info.smart_tools.smartactors.actors.validate_form_data.ValidateFormDataActor;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

/**
 *
 */
public class ValidationFormActorPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap
     */
    public ValidationFormActorPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("ValidateFormActorPlugin");
            item
                    .after("IOC")
                    .before("starter")
                    .process(() -> {
                        try {
                            IKey actorKey = Keys.getKeyByName(ValidateFormDataActor.class.getCanonicalName());
                            IOC.register(actorKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> {
                                        try {
                                            return new ValidateFormDataActor((IObject) args[0]);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecutionException("ValidateFormActor plugin can't load: can't get ValidateFormActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecutionException("ValidateFormActor plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecutionException("ValidateFormActor plugin can't load: can't register new strategy", e);
                        }
                    });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load ValidateFormActor plugin", e);
        }
    }
}
