package info.smart_tools.smartactors.core.plugin_create_async_operation_task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.function.Function;

public class CreateAsyncOperationTaskPlugin implements IPlugin {
    private IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public CreateAsyncOperationTaskPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IKey createAsyncOperationTaskKey = Keys.getOrAdd(CreateAsyncOperationTask.class.toString());
            IBootstrapItem<String> item = new BootstrapItem("CreateAsyncOperationTaskPlugin");

            item.process(() -> {
                try {
                    IOC.register(createAsyncOperationTaskKey, new CreateNewInstanceStrategy((args) -> {
                        try {
                            return new CreateAsyncOperationTask((IDatabaseTask) args[0]);
                        } catch (CreateAsyncOperationException e) {
                            throw new RuntimeException("Target argument must be IDatabaseTask, but was " + args[0].getClass().toString(), e);
                        }
                    }));
                } catch (RegistrationException | InvalidArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
            bootstrap.add(item);
        } catch (ResolutionException | InvalidArgumentException e) {
            throw new PluginException("Can't load CreateAsyncOperationTask plugin", e);
        }
    }
}
