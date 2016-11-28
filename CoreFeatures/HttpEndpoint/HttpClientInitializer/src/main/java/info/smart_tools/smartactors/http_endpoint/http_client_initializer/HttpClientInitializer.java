package info.smart_tools.smartactors.http_endpoint.http_client_initializer;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.IEnvironmentHandler;
import info.smart_tools.smartactors.endpoint.interfaces.ienvironment_handler.exception.EnvironmentHandleException;
import info.smart_tools.smartactors.http_endpoint.environment_handler.EnvironmentHandler;
import info.smart_tools.smartactors.http_endpoint.http_client.HttpClient;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimer;
import info.smart_tools.smartactors.timer.interfaces.itimer.ITimerTask;
import info.smart_tools.smartactors.timer.interfaces.itimer.exceptions.TaskScheduleException;

import java.util.HashMap;
import java.util.Map;

/**
 * Strategy initializer for {@link HttpClient}
 */
public class HttpClientInitializer {
    public static void init(final Integer stackDepth) throws InvalidArgumentException, ResolutionException, RegistrationException {
        IFieldName uuidFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "uuid");
        IFieldName timeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "timeout");
        IFieldName messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");

        IQueue queue = IOC.resolve(Keys.getOrAdd("task_queue"));
        IEnvironmentHandler environmentHandler =  new EnvironmentHandler(queue, stackDepth);
        Map<Object, ITimerTask> timerTasks = new HashMap<>();
        IOC.register(Keys.getOrAdd("createTimerOnRequest"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            ITimer timer =
                                    null;
                            IObject message = (IObject) args[0];
                            Object chainName = args[1];

                            try {
                                IObject environment = IOC.resolve(Keys.getOrAdd("EmptyIObject"));
                                environment.setValue(messageFieldName, message);
                                IChainStorage chainStorage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(),
                                        IChainStorage.class.getCanonicalName()));
                                Object mapId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), chainName);
                                IReceiverChain chain = chainStorage.resolve(mapId);
                                ITask task = () -> {
                                    try {
                                        environmentHandler.handle(environment, chain, null);
                                        ITimerTask timerTask = timerTasks.get(message.getValue(uuidFieldName));
                                        timerTask.cancel();
                                        timerTasks.remove(message.getValue(uuidFieldName));
                                    } catch (ReadValueException | InvalidArgumentException |
                                            EnvironmentHandleException e) {
                                        throw new RuntimeException(e);
                                    }
                                };
                                timer = IOC.resolve(Keys.getOrAdd("timer"));
                                ITimerTask timerTask =
                                        timer.schedule(task,
                                                System.currentTimeMillis() + Long.valueOf(
                                                        String.valueOf(
                                                                message.getValue(timeFieldName)
                                                        )
                                                )
                                        );
                                timerTasks.put(message.getValue(uuidFieldName), timerTask);
                                return timerTask;
                            } catch (ResolutionException | TaskScheduleException | ReadValueException |
                                    ChangeValueException | ChainNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );

        IOC.register(Keys.getOrAdd("cancelTimerOnRequest"), new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            Object uuid = args[0];
                            ITimerTask timerTask = timerTasks.get(uuid);
                            timerTask.cancel();
                            timerTasks.remove(uuid);
                            return null;
                        }
                )
        );
    }
}
