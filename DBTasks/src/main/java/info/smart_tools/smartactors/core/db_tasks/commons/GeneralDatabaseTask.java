package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.IDatabaseTask;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.db_tasks.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.db_tasks.wrappers.upsert.IUpsertMessage;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

abstract class GeneralDatabaseTask implements IDatabaseTask {
    private ICompiledQuery query;
    private IObject message;
    private IStorageConnection connection;
    private boolean isNonExecutable;

    private Map<Boolean, IPrepareStrategy> prepareStrategies;
    private Map<Boolean, IExecuteStrategy> executeStrategies;

    /**
     *
     */
    protected GeneralDatabaseTask() {
        prepareStrategies = new HashMap<>(2);
        prepareStrategies.put(true, message -> setInternalState(null , message));
        prepareStrategies.put(false, message -> {
            ICompiledQuery compiledQuery = takeQuery(connection, message);
            setInternalState(setParameters(compiledQuery, message), message);
        });

        executeStrategies = new HashMap<>(2);
        executeStrategies.put(true, () -> { });
        executeStrategies.put(false, () -> execute(query, message));
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
            prepareStrategies.get(isNonExecutable).doAction(insertMessage);
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
            executeStrategies.get(isNonExecutable).doAction();
        } catch (Exception e) {
            throw new TaskExecutionException("Task execution has been failed because:" + e.getMessage(), e);
        }
    }

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#setStorageConnection(IStorageConnection)}
     *
     * @param storageConnection - {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setStorageConnection(IStorageConnection)}.
     *
     * @throws TaskSetConnectionException {@see IDatabaseTask}
     *                  {@link IDatabaseTask#setStorageConnection(IStorageConnection)}
     */
    @Override
    public void setStorageConnection(final IStorageConnection storageConnection) throws TaskSetConnectionException {
        connection = storageConnection;
    }


    protected abstract @Nonnull
    ICompiledQuery takeQuery(
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
    private interface IPrepareStrategy {
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
    private interface IExecuteStrategy {
        /**
         *
         * @throws TaskExecutionException
         */
        void doAction() throws TaskExecutionException;
    }
}
