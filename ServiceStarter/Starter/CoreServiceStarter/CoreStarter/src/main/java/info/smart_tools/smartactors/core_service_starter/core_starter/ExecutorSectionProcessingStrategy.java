package info.smart_tools.smartactors.core_service_starter.core_starter;

import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask_dispatcher.ITaskDispatcher;
import info.smart_tools.smartactors.task.interfaces.ithread_pool.IThreadPool;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.task.task_dispatcher.TaskDispatcher;
import info.smart_tools.smartactors.task.thread_pool.ThreadPool;

/**
 *
 */
public class ExecutorSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    private final IFieldName threadCountFieldName;
    private final IFieldName maxRunningThreadsFieldName;
    private final IFieldName maxExecutionDelayFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public ExecutorSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "executor");
        this.threadCountFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "threadCount");
        this.maxRunningThreadsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maxRunningThreads");
        this.maxExecutionDelayFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maxExecutionDelay");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            IObject section = (IObject) config.getValue(name);
            int threadsCount = Integer.valueOf(String.valueOf(section.getValue(threadCountFieldName)));
            int maxRunningThreads = Integer.valueOf(String.valueOf(section.getValue(maxRunningThreadsFieldName)));
            int maxExecutionDelay = Integer.valueOf(String.valueOf(section.getValue(maxExecutionDelayFieldName)));

            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd(IQueue.class.getCanonicalName()), section);

            IThreadPool threadPool = new ThreadPool(threadsCount);

            ITaskDispatcher taskDispatcher = new TaskDispatcher(queue, threadPool, maxExecutionDelay, maxRunningThreads);

            IOC.register(Keys.getOrAdd("task_dispatcher"), new SingletonStrategy(taskDispatcher));
            IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(queue));
            IOC.register(Keys.getOrAdd("thread_pool"), new SingletonStrategy(threadPool));

            taskDispatcher.start();
        } catch (InvalidArgumentException | ResolutionException | RegistrationException | ReadValueException e) {
            throw new ConfigurationProcessingException(e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
