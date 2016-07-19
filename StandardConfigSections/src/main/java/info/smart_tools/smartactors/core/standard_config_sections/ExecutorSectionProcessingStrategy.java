package info.smart_tools.smartactors.core.standard_config_sections;

import info.smart_tools.smartactors.core.blocking_queue.BlockingQueue;
import info.smart_tools.smartactors.core.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.core.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iqueue.IQueue;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask_dispatcher.ITaskDispatcher;
import info.smart_tools.smartactors.core.ithread_pool.IThreadPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.task_dispatcher.TaskDispatcher;
import info.smart_tools.smartactors.core.thread_pool.ThreadPool;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 */
public class ExecutorSectionProcessingStrategy implements ISectionStrategy {
    private final IFieldName name;

    private final IFieldName threadCountFieldName;
    private final IFieldName maxRunningThreadsFieldName;
    private final IFieldName maxExecutionDelayFieldName;
    private final IFieldName queueSizeFieldName;

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
        this.queueSizeFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "queueSize");
    }

    @Override
    public void onLoadConfig(final IObject config) throws ConfigurationProcessingException {
        try {
            IObject section = (IObject) config.getValue(name);
            int threadsCount = Integer.valueOf(String.valueOf(section.getValue(threadCountFieldName)));
            int maxRunningThreads = Integer.valueOf(String.valueOf(section.getValue(maxRunningThreadsFieldName)));
            int maxExecutionDelay = Integer.valueOf(String.valueOf(section.getValue(maxExecutionDelayFieldName)));
            int queueSize = Integer.valueOf(String.valueOf(section.getValue(queueSizeFieldName)));

            IQueue<ITask> queue = new BlockingQueue<>(new ArrayBlockingQueue<>(queueSize));

            IThreadPool threadPool = new ThreadPool(threadsCount);

            ITaskDispatcher taskDispatcher = new TaskDispatcher(queue, threadPool, maxExecutionDelay, maxRunningThreads);

            IOC.register(Keys.getOrAdd("task_dispatcher"), new SingletonStrategy(taskDispatcher));
            IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(queue));
            IOC.register(Keys.getOrAdd("thread_pool"), new SingletonStrategy(threadPool));
        } catch (InvalidArgumentException | ResolutionException | RegistrationException | ReadValueException e) {
            throw new ConfigurationProcessingException(e);
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
