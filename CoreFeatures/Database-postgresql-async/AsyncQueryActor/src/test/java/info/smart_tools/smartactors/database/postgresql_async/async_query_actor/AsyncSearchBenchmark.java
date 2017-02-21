package info.smart_tools.smartactors.database.postgresql_async.async_query_actor;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl.JSONBDataConverter;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.SearchMessage;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject_plugins.dsobject_plugin.PluginDSObject;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc_plugins.ioc_keys_plugin.PluginIOCKeys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.scope_plugins.scope_provider_plugin.PluginScopeProvider;
import info.smart_tools.smartactors.scope_plugins.scoped_ioc_plugin.ScopedIOCPlugin;
import info.smart_tools.smartactors.task.blocking_queue.BlockingQueue;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;

import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 */
public class AsyncSearchBenchmark {
    private final static String COLLECTION_NAME = "testcollection1";
    private final static int QUEUE_SIZE  = 100000;
    private final static int QUERY_COUNT = 10000;

    private static long startTime;
    private static long nWaitingProcessors = 0;
    private static long pureAsyncExecTime;

    private static class MessageProcessor implements IMessageProcessor {
        @Override
        public IObject getMessage() {
            return null;
        }

        @Override
        public IObject getContext() {
            return null;
        }

        @Override
        public IObject getResponse() {
            return null;
        }

        @Override
        public IMessageProcessingSequence getSequence() {
            return null;
        }

        @Override
        public IObject getEnvironment() {
            return null;
        }

        @Override
        public void setConfig(IObject config) throws InvalidArgumentException {

        }

        @Override
        public void process(IObject message, IObject context) throws InvalidArgumentException, ResolutionException, ChangeValueException {

        }

        @Override
        public void pauseProcess() throws AsynchronousOperationException {
            ++nWaitingProcessors;
        }

        @Override
        public void continueProcess(Throwable e) throws AsynchronousOperationException {
            if (null != e) {
                e.printStackTrace();
            }

            if (nWaitingProcessors % (QUERY_COUNT / 100) == 0) {
                System.out.println(MessageFormat.format("* Got {0} of {1} response(s) after {2} ms ({3} exclusive async ms)",
                        QUERY_COUNT - nWaitingProcessors, QUERY_COUNT, System.currentTimeMillis() - startTime, pureAsyncExecTime));
            }

            --nWaitingProcessors;
        }
    }

    private static class TestQueryMessage implements SearchMessage {
        private final IMessageProcessor messageProcessor;
        private final IObject query;

        TestQueryMessage(final IMessageProcessor messageProcessor, final IObject query) {
            this.messageProcessor = messageProcessor;
            this.query = query;
        }

        @Override
        public IMessageProcessor getProcessor() throws ReadValueException {
            return messageProcessor;
        }

        @Override
        public Object getQuery() throws ReadValueException {
            return query;
        }

        @Override
        public String getCollectionName() throws ReadValueException {
            return COLLECTION_NAME;
        }

        @Override
        public void setResult(Object result) throws ChangeValueException {

        }
    }

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new ScopedIOCPlugin(bootstrap).load();
        new PluginScopeProvider(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();

        bootstrap.start();

        IQueue<ITask> taskQueue = new BlockingQueue<>(new ArrayBlockingQueue<ITask>(QUEUE_SIZE));
        IOC.register(Keys.getOrAdd("task_queue"), new SingletonStrategy(taskQueue));

        Db db = new ConnectionPoolBuilder()
                .hostname("localhost")
                .port(5433)
                .database("postgres")
                .username("test_user")
                .password("password")
                .poolSize(16)
                .dataConverter(JSONBDataConverter.INSTANCE)
                .build();

        AsyncQueryActor actor = new AsyncQueryActor(db);


        IMessageProcessor mp = new MessageProcessor();
        SearchMessage m = new TestQueryMessage(mp, IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()),
                ("{\n" +
                "        \"filter\": {\n" +
                "            \"id\": {\n" +
                "                \"$lt\": 40\n" +
                "            }\n" +
                "        },\n" +
                "        \"page\": {\n" +
                "            \"size\": 10,\n" +
                "            \"number\": 4\n" +
                "        },\n" +
                "        \"sort\": []\n" +
                "    }")));

        startTime = System.currentTimeMillis();

        for (int i = 0; i < QUERY_COUNT; i++) {
            actor.search(m);
        }

        long syncEndTime = System.currentTimeMillis();

        System.out.println(MessageFormat.format("Spent {0} ms for synchronous tasks.", syncEndTime - startTime));

        pureAsyncExecTime = 0;

        long nTasks = 0;

        while (nWaitingProcessors > 0) {
            ITask task = taskQueue.take();

            long execStart = System.currentTimeMillis();

            task.execute();

            pureAsyncExecTime += (System.currentTimeMillis() - execStart);
            ++nTasks;
        }

        long asyncEndTime = System.currentTimeMillis();

        System.out.println(MessageFormat.format("Spent {0} ms for {1} asynchronous tasks.", asyncEndTime - syncEndTime, nTasks));
        System.out.println(MessageFormat.format("     ({0} ms for tasks themselves)", pureAsyncExecTime));
        System.out.println(MessageFormat.format("Spent {0} ms total.", asyncEndTime - startTime));
        System.out.println(MessageFormat.format("     ({0} ms pure time)", syncEndTime - startTime + pureAsyncExecTime));
    }
}
