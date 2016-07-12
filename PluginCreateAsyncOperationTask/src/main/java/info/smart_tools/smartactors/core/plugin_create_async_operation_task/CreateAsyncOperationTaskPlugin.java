package info.smart_tools.smartactors.core.plugin_create_async_operation_task;

import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.DeleteAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.exception.UpdateAsyncOperationException;
import info.smart_tools.smartactors.core.async_operation_collection.task.CreateAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.DeleteAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.GetAsyncOperationTask;
import info.smart_tools.smartactors.core.async_operation_collection.task.UpdateAsyncOperationTask;
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
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;

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
            IBootstrapItem<String> item = new BootstrapItem("CreateAsyncOperationTaskPlugin");

            item.process(() -> {
                try {
                    IKey createOperationTasksKey = Keys.getOrAdd(CreateAsyncOperationTask.class.toString());
                    IOC.register(createOperationTasksKey, new CreateNewInstanceStrategy((args) -> {
                        try {
                            return new CreateAsyncOperationTask((IDatabaseTask) args[0]);
                        } catch (CreateAsyncOperationException e) {
                            throw new RuntimeException("Target argument must be IDatabaseTask, but was " + args[0].getClass().toString(), e);
                        }
                    }));
                    IKey getOperationTasksKey = Keys.getOrAdd(GetAsyncOperationTask.class.toString());
                    IOC.register(getOperationTasksKey, new CreateNewInstanceStrategy((args) -> {
                        try {
                            return new GetAsyncOperationTask((IDatabaseTask) args[0]);
                        } catch (GetAsyncOperationException e) {
                            throw new RuntimeException("Target argument must be IDatabaseTask, but was " + args[0].getClass().toString(), e);
                        }
                    }));
                    IKey updateOperationTasksKey = Keys.getOrAdd(UpdateAsyncOperationTask.class.toString());
                    IOC.register(updateOperationTasksKey, new CreateNewInstanceStrategy((args) -> {
                        try {
                            return new UpdateAsyncOperationTask((IDatabaseTask) args[0]);
                        } catch (UpdateAsyncOperationException e) {
                            throw new RuntimeException("Target argument must be IDatabaseTask, but was " + args[0].getClass().toString(), e);
                        }
                    }));
                    IKey deleteOperationTasksKey = Keys.getOrAdd(DeleteAsyncOperationTask.class.toString());
                    IOC.register(deleteOperationTasksKey, new CreateNewInstanceStrategy((args) -> {
                        return new DeleteAsyncOperationTask((IDatabaseTask) args[0]);
                    }));
                } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                    throw new RuntimeException(e);
                }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load CreateAsyncOperationTask plugin", e);
        }
    }
}
