package info.smart_tools.smartactors.core.db_task.upsert;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.annotation.Nonnull;

abstract class DBUpsert implements IDatabaseTask {
    private CompiledQuery query;
    private IUpsertQueryMessage message;
    private StorageConnection connection;

    protected DBUpsert() {}

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     * Prepare task a insert documents query for postgres database.

     * @param insertMessage - contains parameters for insert query.
     *          {@see IInsertMessage}:
     *                      {@link IUpsertQueryMessage#getCollectionName()},
     *
     * @throws TaskPrepareException {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     */
    @Override
    public void prepare(final IObject insertMessage) throws TaskPrepareException {
        try {
            checkConnection(connection);
            IUpsertQueryMessage queryMessage = takeMessageWrapper(insertMessage);
            if (requiresExit(message)) {
                setInternalState(null , queryMessage);
                return;
            }
            CompiledQuery compiledQuery = takeQuery(connection, queryMessage);
            setInternalState(formatQuery(compiledQuery, queryMessage), queryMessage);
        } catch (QueryBuildException | ResolutionException |
                TaskSetConnectionException | ReadValueException e) {
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
            if (requiresExit(message)) {
                return;
            }
            checkExecutionConditions();
            execute(query, message);
        } catch (ReadValueException e) {
            throw new TaskExecutionException("Task execution has been failed because:" + e.getMessage(), e);
        }
    }

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#setStorageConnection(StorageConnection)}
     *
     * @param storageConnection - {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setStorageConnection(StorageConnection)}.
     *
     * @throws TaskSetConnectionException {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setStorageConnection(StorageConnection)}
     */
    public void setStorageConnection(final StorageConnection storageConnection) throws TaskSetConnectionException {
        checkConnection(storageConnection);
        connection = storageConnection;
    }

    protected abstract CompiledQuery takeQuery(
            @Nonnull final StorageConnection connection,
            @Nonnull final IUpsertQueryMessage queryMessage
    ) throws QueryBuildException;

    protected abstract CompiledQuery formatQuery(
            @Nonnull final CompiledQuery query,
            @Nonnull final IUpsertQueryMessage message
    ) throws QueryBuildException;

    protected abstract void execute(
            @Nonnull final CompiledQuery compiledQuery,
            @Nonnull final IUpsertQueryMessage queryMessage
    ) throws TaskExecutionException;

    private IUpsertQueryMessage takeMessageWrapper(final IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IUpsertQueryMessage.class.toString()),
                object);
    }

    private void setInternalState(final CompiledQuery query, final IUpsertQueryMessage message) {
        this.query = query;
        this.message = message;
    }

    private boolean requiresExit(final IUpsertQueryMessage queryMessage) throws ReadValueException {
        return queryMessage != null && queryMessage.getDocument() == null;
    }

    private void checkExecutionConditions() throws TaskExecutionException {
        if (query == null || message == null) {
            throw new TaskExecutionException("Should first prepare the task.");
        }
    }

    private void checkConnection(final StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null) {
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        }
        if (connection.getId() == null || connection.getId().isEmpty()) {
            throw new TaskSetConnectionException("Connection should have an id!");
        }
    }
}
