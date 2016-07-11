package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.IDatabaseTask;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.db_tasks.wrappers.upsert.IUpsertMessage;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public abstract class GeneralDatabaseTask implements IDatabaseTask {
    private ICompiledQuery query;
    private IObject message;
    private IStorageConnection connection;
    private boolean isNonExecutable;

    private final Map<Boolean, IPreparationStrategy> preparationStrategies;
    private final Map<Boolean, IExecutionStrategy> executionStrategies;

    private static final ConcurrentMap<IKey, ICompiledQuery> CACHED_COMPILED_QUERIES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, IField> CACHED_DOCUMENTS_IDS = new ConcurrentHashMap<>();

    /**
     *
     */
    protected GeneralDatabaseTask() {
        preparationStrategies = new HashMap<>(2);
        preparationStrategies.put(true, message -> { });
        preparationStrategies.put(false, message -> {
            ICompiledQuery compiledQuery = takeCompiledQuery(connection, message);
            setInternalState(setParameters(compiledQuery, message), message);
        });

        executionStrategies = new HashMap<>(2);
        executionStrategies.put(true, () -> { });
        executionStrategies.put(false, () -> execute(query, message));
    }

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     * Prepare task a insert documents query for postgres database.

     * @param insertMessage - contains parameters for insert query.
     *          {@see IInsertMessage}:
     *                      {@link IUpsertMessage#getCollection()},
     *
     * @throws TaskPrepareException {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     */
    @Override
    public void prepare(@Nonnull final IObject insertMessage) throws TaskPrepareException {
        try {
            isNonExecutable = requiresNonExecutable(insertMessage);
            preparationStrategies.get(isNonExecutable).doAction(insertMessage);
        } catch (NullPointerException e) {
            throw new TaskPrepareException("Can't prepare query because: Invalid given insert message!");
        } catch (Exception e) {
            throw new TaskPrepareException("Can't prepare query because: " + e.getMessage(), e);
        }
    }

    /**
     * {@see ITask} {@link ITask#execute()}
     * Executes a insert documents query to postgres database.
     *
     * @throws TaskExecutionException {@see ITask} {@link ITask#execute()}
     */
    @Override
    public void execute() throws TaskExecutionException {
        try {
            executionStrategies.get(isNonExecutable).doAction();
        } catch (Exception e) {
            throw new TaskExecutionException("Task execution has been failed because:" + e.getMessage(), e);
        }
    }

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#setConnection(IStorageConnection)}
     *
     * @param storageConnection - {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setConnection(IStorageConnection)}.
     *
     * @throws TaskSetConnectionException {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setConnection(IStorageConnection)}
     */
    @Override
    public void setConnection(final IStorageConnection storageConnection) {
        connection = storageConnection;
    }


    protected ICompiledQuery takeCompiledQuery(final IKey queryKey,
                                               final IStorageConnection connection,
                                               final IQueryStatementFactory factory
    ) throws QueryBuildException {
        ICompiledQuery compiledQuery = CACHED_COMPILED_QUERIES.get(queryKey);
        if (compiledQuery != null) {
            return compiledQuery;
        }
        compiledQuery = createCompiledQuery(connection, factory);
        CACHED_COMPILED_QUERIES.put(queryKey, compiledQuery);

        return compiledQuery;
    }

    protected ICompiledQuery createCompiledQuery(final IStorageConnection connection,
                                                 final IQueryStatementFactory factory
    ) throws QueryBuildException {
        try {
            QueryStatement queryStatement = factory.create();
            return connection.compileQuery(queryStatement);
        } catch (QueryStatementFactoryException | StorageException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    protected IField getIdFieldFor(final String collection) throws ResolutionException {
        IField id = CACHED_DOCUMENTS_IDS.get(collection);
        if (id == null) {
            id = IOC.resolve(Keys.getOrAdd(IField.class.toString()), collection + "Id");
            CACHED_DOCUMENTS_IDS.put(collection, id);
        }

        return id;
    }

    @Override
    public IStorageConnection getConnection() {
        return connection;
    }

    protected abstract @Nonnull
    ICompiledQuery takeCompiledQuery(
            @Nonnull final IStorageConnection connection,
            @Nonnull final IObject queryMessage
    ) throws QueryBuildException;

    protected abstract @Nonnull
    ICompiledQuery setParameters(
            @Nonnull final ICompiledQuery query,
            @Nonnull final IObject message
    ) throws QueryBuildException;

    protected abstract void execute(
            @Nonnull final ICompiledQuery compiledQuery,
            @Nonnull final IObject queryMessage
    ) throws TaskExecutionException;

    protected abstract boolean requiresNonExecutable(@Nonnull final IObject queryMessage)
            throws InvalidArgumentException;


    /* Internal method. */
    private void setInternalState(final ICompiledQuery compiledQuery, final IObject queryMessage) {
        this.query = compiledQuery;
        this.message = queryMessage;
    }

    /**
     *
     */
    @FunctionalInterface
    private interface IPreparationStrategy {
        /**
         *
         * @param message
         * @throws QueryBuildException
         */
        void doAction(final IObject message) throws QueryBuildException;
    }

    /**
     *
     */
    @FunctionalInterface
    private interface IExecutionStrategy {
        /**
         *
         * @throws TaskExecutionException
         */
        void doAction() throws TaskExecutionException;
    }
}
