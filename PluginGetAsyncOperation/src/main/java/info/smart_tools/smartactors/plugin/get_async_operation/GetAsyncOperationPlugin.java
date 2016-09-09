package info.smart_tools.smartactors.plugin.get_async_operation;

import info.smart_tools.smartactors.actors.get_async_operation.GetAsyncOperationActor;
import info.smart_tools.smartactors.actors.get_async_operation.exception.GetAsyncOperationActorException;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.util.Arrays;

/**
 * Plugin for {@link GetAsyncOperationActor}
 */
public class GetAsyncOperationPlugin implements IPlugin {

    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor for GetAsyncOperationPlugin
     * @param bootstrap the bootstrap
     */
    public GetAsyncOperationPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateGetAsyncOperationActor");

            item
                    .after("IOC")
                    .before("starter")
                    .process(() -> {
                        try {
                            IKey actorKey = Keys.getOrAdd(GetAsyncOperationActor.class.getCanonicalName());
                            IOC.register(actorKey, new ApplyFunctionToArgumentsStrategy(
                                    (args) -> {
                                        try {
                                            return new GetAsyncOperationActor((IObject) args[0]);
                                        } catch (GetAsyncOperationActorException e) {
                                            throw new RuntimeException("Can't create GetAsyncOperationActor with params: " + Arrays.toString(args), e);
                                        }
                                    }
                            ));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("GetAsyncOperationActor plugin can't load: can't get GetAsyncOperationActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("GetAsyncOperationActor plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("GetAsyncOperationActor plugin can't load: can't register new strategy", e);
                        }
                    });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load GetAsyncOperationPlugin", e);
        }

    }
}
