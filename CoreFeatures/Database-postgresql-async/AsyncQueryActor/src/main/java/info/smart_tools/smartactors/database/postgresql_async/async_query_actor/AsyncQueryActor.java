package info.smart_tools.smartactors.database.postgresql_async.async_query_actor;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl.AsyncQueryStatement;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl.JSONBDataConverter;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.ModificationMessage;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.SearchMessage;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.PostgresSchema;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import rx.Observable;
import rx.functions.Action1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AsyncQueryActor {
    private final Db db;
    private final IQueue<ITask> taskQueue;

    /**
     * Task that deserializes documents from query result set, stores them in message and continues message processor execution.
     */
    private class ResultSetDeserializationTask implements ITask {
        private final ResultSet resultSet;
        private final IMessageProcessor messageProcessor;
        private final SearchMessage message;

        ResultSetDeserializationTask(
                final ResultSet resultSet,
                final IMessageProcessor messageProcessor,
                final SearchMessage message) {
            this.resultSet = resultSet;
            this.messageProcessor = messageProcessor;
            this.message = message;
        }

        @Override
        public void execute() throws TaskExecutionException {
            try {
                List<IObject> docs = new ArrayList<>(resultSet.size());

                for (int i = 0; i < resultSet.size(); i++) {
                    String serializedDoc = new String(resultSet.row(i).getBytes(0));
                    IObject deserialized = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), serializedDoc);
                    docs.add(deserialized);
                }

                message.setResult(docs);

                messageProcessor.continueProcess(null);
            } catch (Exception e) {
                try {
                    messageProcessor.continueProcess(e);
                } catch (AsynchronousOperationException ee) {
                    e.addSuppressed(ee);
                }

                throw new TaskExecutionException(e);
            }
        }
    }

    private static void subscribeOnResultSet(
            final Observable<ResultSet> o,
            final IMessageProcessor mp,
            final Action1<ResultSet> action
    ) throws Exception {
        try {
            o.subscribe(
                    action,
                    err -> {
                        try {
                            mp.continueProcess(err);
                        } catch (AsynchronousOperationException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (Exception e) {
            try {
                mp.continueProcess(e);
            } catch (AsynchronousOperationException ee) {
                e.addSuppressed(ee);
            }
            throw e;
        }
    }

    private static void subscribeOnResultSet(
            final Observable<ResultSet> o,
            final IMessageProcessor mp) throws Exception {
        subscribeOnResultSet(
                o, mp, set -> {
                    try {
                        mp.continueProcess(null);
                    } catch (AsynchronousOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static final int TEST_MAGIC_VALUE = 42;

    private void testConnection()
            throws Exception {
        try {
            int test = db.querySet(String.format("select %s", TEST_MAGIC_VALUE))
                    .toBlocking()
                    .last().row(0).getInt(0);

            if (test != TEST_MAGIC_VALUE) {
                throw new Exception(String.format("Unexpected result: %s (expected: %s).", test, TEST_MAGIC_VALUE));
            }
        } catch (Exception e) {
            throw new Exception("Connection test failed.", e);
        }
    }

    /**
     * @param uri      database URI: pgsql:///user:password@host/database
     * @param poolSize size of connection pool
     * @throws Exception if any error occurs. Really. I cannot know what may throw the 3-rd party library this actor uses.
     */
    // TODO: Use ConnectionOptions
    public AsyncQueryActor(final URI uri, final int poolSize)
            throws Exception {
        String[] userInfo = uri.getUserInfo().split(":");

        ConnectionPoolBuilder builder = new ConnectionPoolBuilder()
                .hostname(uri.getHost())
                .port(uri.getPort())
                .database(uri.getPath().substring(1)) // skip "^/"
                .username(userInfo[0])
                .poolSize(poolSize);

        if (userInfo.length > 1) {
            builder = builder.password(userInfo[1]);
        }

        builder = builder.dataConverter(JSONBDataConverter.INSTANCE);

        db = builder.build();

        testConnection();

        taskQueue = IOC.resolve(Keys.getOrAdd("task_queue"));
    }

    /**
     * Insert a document.
     *
     * @param message the message
     * @throws Exception if any error occurs
     */
    public void insert(final ModificationMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();
        IObject document = message.getDocument();

        // Generate and set document ID
        IFieldName idField = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                String.format(PostgresSchema.ID_FIELD_PATTERN, message.getCollectionName()));
        document.setValue(idField, IOC.resolve(Keys.getOrAdd("db.collection.nextid")));

        String serializedDoc = document.serialize();

        PostgresSchema.insert(statement, CollectionName.fromString(message.getCollectionName()));

        statement.pushParameterSetter((parameters, firstIndex) -> {
            parameters.setString(firstIndex, serializedDoc);
            return firstIndex + 1;
        });

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        subscribeOnResultSet(resObservable, mp);
    }

    /**
     * Search for douments.
     *
     * @see PostgresSchema#search(QueryStatement, CollectionName, IObject)
     * @param message the message
     * @throws Exception if any error occurs
     */
    public void search(final SearchMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();

        PostgresSchema.search(statement, CollectionName.fromString(message.getCollectionName()), (IObject) message.getQuery());

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        subscribeOnResultSet(
                resObservable,
                mp,
                set -> {
                    try {
                        taskQueue.put(new ResultSetDeserializationTask(set, mp, message));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        try {
                            mp.continueProcess(e);
                        } catch (AsynchronousOperationException ee) {
                            throw new RuntimeException(ee);
                        }
                    }
                }
        );
    }

    /**
     * Query count of documents matching a criteria.
     *
     * @param message the message
     * @throws Exception if any error occurs
     */
    public void count(final SearchMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();

        PostgresSchema.count(statement, CollectionName.fromString(message.getCollectionName()), (IObject) message.getQuery());

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        IScope scope = ScopeProvider.getCurrentScope();

        subscribeOnResultSet(
                resObservable,
                mp,
                set -> {
                    try {
                        ScopeProvider.setCurrentScope(scope);
                        message.setResult(set.row(0).getBigInteger(0));
                        mp.continueProcess(null);
                    } catch (Exception e) {
                        try {
                            mp.continueProcess(e);
                        } catch (AsynchronousOperationException ee) {
                            e.addSuppressed(ee);
                        }

                        throw new RuntimeException(e);
                    }
                }
        );
    }

    /**
     * Update a document.
     *
     * @param message    the message
     * @throws Exception if any error occurs
     */
    public void update(final ModificationMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();

        PostgresSchema.update(statement, CollectionName.fromString(message.getCollectionName()));

        String serializedDoc = message.getDocument().serialize();
        IFieldName idFieldName = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                String.format(PostgresSchema.ID_FIELD_PATTERN, message.getCollectionName()));
        Object docId = message.getDocument().getValue(idFieldName);

        statement.pushParameterSetter((stmt, firstIndex) -> {
            stmt.setString(firstIndex++, serializedDoc);
            stmt.setObject(firstIndex++, docId);
            return firstIndex;
        });

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        subscribeOnResultSet(resObservable, mp);
    }

    /**
     * Delete the document.
     *
     * @param message    the message
     * @throws Exception if any error occurs
     */
    public void delete(final ModificationMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();

        PostgresSchema.delete(statement, CollectionName.fromString(message.getCollectionName()));

        IFieldName idFieldName = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                String.format(PostgresSchema.ID_FIELD_PATTERN, message.getCollectionName()));
        Object docId = message.getDocument().getValue(idFieldName);

        statement.pushParameterSetter((stmt, firstIndex) -> {
            stmt.setObject(firstIndex++, docId);
            return firstIndex;
        });

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        subscribeOnResultSet(resObservable, mp);
    }

    /**
     * Update or insert the document.
     *
     * @param message    the message
     * @throws Exception if any error occurs
     */
    public void upsert(final ModificationMessage message)
            throws Exception {
        IFieldName idFieldName = IOC.resolve(
                Keys.getOrAdd(IFieldName.class.getCanonicalName()),
                String.format(PostgresSchema.ID_FIELD_PATTERN, message.getCollectionName()));
        if (null == message.getDocument().getValue(idFieldName)) {
            insert(message);
        } else {
            update(message);
        }
    }

    /**
     * Get document by it's identifier.
     *
     * @param message    the message
     * @throws Exception if any error occurs
     */
    public void getById(final SearchMessage message)
            throws Exception {
        AsyncQueryStatement statement = new AsyncQueryStatement();
        Object id = message.getQuery();

        PostgresSchema.getById(statement, CollectionName.fromString(message.getCollectionName()));

        statement.pushParameterSetter((params, firstIndex) -> {
            params.setObject(firstIndex++, id);
            return firstIndex;
        });

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        subscribeOnResultSet(
                resObservable,
                mp,
                set -> {
                    try {
                        taskQueue.put(new ResultSetDeserializationTask(set, mp, message));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        try {
                            mp.continueProcess(e);
                        } catch (AsynchronousOperationException ee) {
                            throw new RuntimeException(ee);
                        }
                    }
                }
        );
    }
}
