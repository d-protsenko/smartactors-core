package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.IDatabaseTask;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.db_tasks.wrappers.IDBTaskMessage;
import info.smart_tools.smartactors.core.db_tasks.wrappers.upsert.IUpsertMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

abstract class GeneralDatabaseTask<TMessage extends IDBTaskMessage> implements IDatabaseTask {
    private CompiledQuery query;
    private TMessage message;
    private StorageConnection connection;

    protected GeneralDatabaseTask() {}

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
    public void prepare(final IObject insertMessage) throws TaskPrepareException {
        try {
            checkConnection(connection);
            TMessage queryMessage = takeMessageWrapper(insertMessage);
            if (requiresExit(message)) {
                setInternalState(null , queryMessage);
                return;
            }
            CompiledQuery compiledQuery = takeQuery(connection, queryMessage);
            setInternalState(setParameters(compiledQuery, queryMessage), queryMessage);
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
            if (requiresExit(message)) {
                return;
            }
            checkExecutionConditions();
            execute(query, message);
        } catch (Exception e) {
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
    @Override
    public void setStorageConnection(final StorageConnection storageConnection) throws TaskSetConnectionException {
        checkConnection(storageConnection);
        connection = storageConnection;
    }


    /* @see . */

    protected abstract @Nonnull CompiledQuery takeQuery(
            @Nonnull final StorageConnection connection,
            @Nonnull final TMessage queryMessage
    ) throws QueryBuildException;

    protected abstract @Nonnull CompiledQuery setParameters(
            @Nonnull final CompiledQuery query,
            @Nonnull final TMessage message
    ) throws QueryBuildException;

    protected abstract void execute(
            @Nonnull final CompiledQuery compiledQuery,
            @Nonnull final TMessage queryMessage
    ) throws TaskExecutionException;

    protected abstract boolean requiresExit(@Nonnull final TMessage queryMessage) throws InvalidArgumentException;

    protected abstract @Nonnull TMessage takeMessageWrapper(@Nonnull final IObject object) throws ResolutionException;



    /* Internal methods. */

    private void setInternalState(final CompiledQuery query, final TMessage message) {
        this.query = query;
        this.message = message;
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
