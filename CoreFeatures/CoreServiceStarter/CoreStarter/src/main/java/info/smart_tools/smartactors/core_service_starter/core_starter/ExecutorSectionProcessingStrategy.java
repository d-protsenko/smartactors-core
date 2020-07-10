//package info.smart_tools.smartactors.core_service_starter.core_starter;
package info.smart_tools.smartactors.core_service_starter.core_starter;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IActionNoArgs;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.base.iup_counter.IUpCounter;
import info.smart_tools.smartactors.base.iup_counter.exception.UpCounterCallbackExecutionException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.ISectionStrategy;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask_dispatcher.ITaskDispatcher;
import info.smart_tools.smartactors.task.interfaces.ithread_pool.IThreadPool;
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
    private final IFieldName defaultStackDepthFieldName;

    private final int DEFAULT_STACK_DEPTH = 5;

    /**
     * The constructor.
     *
     * @throws ResolutionException if fails to resolve any dependencies
     */
    public ExecutorSectionProcessingStrategy()
            throws ResolutionException {
        this.name = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "executor");
        this.threadCountFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "threadCount");
        this.maxRunningThreadsFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "maxRunningThreads");
        this.maxExecutionDelayFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "maxExecutionDelay");
        this.defaultStackDepthFieldName = IOC.resolve(Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "defaultStackDepth");
    }

    @Override
    public void onLoadConfig(final IObject config)
            throws ConfigurationProcessingException {
        try {
            IObject section = (IObject) config.getValue(name);
            int threadsCount = Integer.valueOf(String.valueOf(section.getValue(threadCountFieldName)));
            int maxRunningThreads = Integer.valueOf(String.valueOf(section.getValue(maxRunningThreadsFieldName)));
            int maxExecutionDelay = Integer.valueOf(String.valueOf(section.getValue(maxExecutionDelayFieldName)));
            Integer defaultStackDepth = (Integer) section.getValue(this.defaultStackDepthFieldName);

            IQueue<ITask> queue = IOC.resolve(Keys.getKeyByName(IQueue.class.getCanonicalName()), section);

            IThreadPool threadPool = new ThreadPool(threadsCount);

            ITaskDispatcher taskDispatcher = new TaskDispatcher(queue, threadPool, maxExecutionDelay, maxRunningThreads);

            IOC.register(Keys.getKeyByName("task_dispatcher"), new SingletonStrategy(taskDispatcher));
            IOC.register(Keys.getKeyByName("task_queue"), new SingletonStrategy(queue));
            IOC.register(Keys.getKeyByName("thread_pool"), new SingletonStrategy(threadPool));
            IOC.register(
                    Keys.getKeyByName("default_stack_depth"),
                    new SingletonStrategy(
                        null != defaultStackDepth ? defaultStackDepth : DEFAULT_STACK_DEPTH
                    )
            );

            taskDispatcher.start();

            IUpCounter rootUpCounter = IOC.resolve(Keys.getKeyByName("root upcounter"));

            rootUpCounter.onShutdownComplete(taskDispatcher.toString(), () -> {
                taskDispatcher.stop();
                threadPool.terminate();
            });
        } catch (InvalidArgumentException | ResolutionException | RegistrationException | ReadValueException
                | UpCounterCallbackExecutionException e) {
            throw new ConfigurationProcessingException(e);
        }
    }

    @Override
    public void onRevertConfig(final IObject config)
            throws ConfigurationProcessingException {
        ConfigurationProcessingException exception = new ConfigurationProcessingException("Error occurred reverting \"Executor\" configuration section.");
        try {
            ITaskDispatcher taskDispatcher = IOC.resolve(Keys.getKeyByName("task_dispatcher"));
            taskDispatcher.stop();
            IUpCounter rootUpCounter = IOC.resolve(Keys.getKeyByName("root upcounter"));
            IActionNoArgs taskDispatcherShutdown = rootUpCounter.removeFromShutdownComplete(taskDispatcher.toString());
            taskDispatcherShutdown.execute();
        } catch (ResolutionException | ActionExecutionException e) {
            exception.addSuppressed(e);
        }
        try {
            IThreadPool threadPool = IOC.resolve(Keys.getKeyByName("thread_pool"));
            threadPool.terminate();
        } catch (ResolutionException e) {
            exception.addSuppressed(e);
        }
        try {
            IOC.unregister(Keys.getKeyByName("default_stack_depth"));
        } catch(ResolutionException | DeletionException e) {
            exception.addSuppressed(e);
        }
        try {
            IOC.unregister(Keys.getKeyByName("thread_pool"));
        } catch(ResolutionException | DeletionException e) {
            exception.addSuppressed(e);
        }
        try {
            IOC.unregister(Keys.getKeyByName("task_queue"));
        } catch(ResolutionException | DeletionException e) {
            exception.addSuppressed(e);
        }
        try {
            IOC.unregister(Keys.getKeyByName("task_dispatcher"));
        } catch(ResolutionException | DeletionException e) {
            exception.addSuppressed(e);
        }
        if (exception.getSuppressed().length > 0) {
            throw exception;
        }
    }

    @Override
    public IFieldName getSectionName() {
        return name;
    }
}
