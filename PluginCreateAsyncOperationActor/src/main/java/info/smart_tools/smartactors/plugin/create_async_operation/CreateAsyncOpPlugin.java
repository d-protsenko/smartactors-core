package info.smart_tools.smartactors.plugin.create_async_operation;

import info.smart_tools.smartactors.actors.create_async_operation.CreateAsyncOperationActor;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Plugin that load actor for creating async operations
 */
public class CreateAsyncOpPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public CreateAsyncOpPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("CreateAsyncOperationActorPlugin");

            item
                    .after("IOC")
                    .after("datetime_formatter_plugin")
                    .before("starter")
                    .process(() -> {
                        try {
                            IKey createAsyncOpKey = Keys.getOrAdd(CreateAsyncOperationActor.class.getCanonicalName());
                            IOC.register(createAsyncOpKey, new CreateNewInstanceStrategy(
                                    (args) -> {
                                        try {
                                            return new CreateAsyncOperationActor((IObject) args[0]);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (ResolutionException e) {
                            throw new ActionExecuteException("CreateAsyncOperationActor plugin can't load: can't get CreateAsyncOperationActor key", e);
                        } catch (InvalidArgumentException e) {
                            throw new ActionExecuteException("CreateAsyncOperationActor plugin can't load: can't create strategy", e);
                        } catch (RegistrationException e) {
                            throw new ActionExecuteException("CreateAsyncOperationActor plugin can't load: can't register new strategy", e);
                        }
                    });
            bootstrap.add(item);
        } catch (Exception e) {
            throw new PluginException("Can't load CreateAsyncOperationActor plugin", e);
        }
    }
}
