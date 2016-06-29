package info.smart_tools.smartactors.core.db_task.create_collection.psql;

import com.sun.istack.internal.NotNull;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.create_collection.psql.wrapper.ICreateCollectionQueryMessage;
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
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import java.util.Map;

/**
 * Task for create collection with predefined indexes in psql database.
 */
public class PSQLCreateCollectionTask implements IDatabaseTask {
    private CompiledQuery compiledQuery;
    private StorageConnection storageConnection;

    /**
     * Default constructor.
     *              Creates a new instance of {@link PSQLCreateCollectionTask}.
     */
    protected PSQLCreateCollectionTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLCreateCollectionTask}.
     *
     * @return a new instance of {@link PSQLCreateCollectionTask}.
     */
    public static PSQLCreateCollectionTask create() {
        return new PSQLCreateCollectionTask();
    }

    /**
     * {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     * Prepare task a create a new collection compiledQuery for psql database.
     *
     * @param createCollectionMessage - contains parameters for create collection compiledQuery.
     *          {@see ICreateCollectionQuery}:
     *                  {@link ICreateCollectionQueryMessage#getCollectionName()},
     *                  {@link ICreateCollectionQueryMessage#getIndexes()}.
     *
     * @throws TaskPrepareException {@see IDatabaseTask} {@link IDatabaseTask#prepare(IObject)}
     */
    @Override
    public void prepare(@NotNull final IObject createCollectionMessage) throws TaskPrepareException {
        try {
            verify(storageConnection);
            ICreateCollectionQueryMessage messageWrapper = takeQueryMessage(createCollectionMessage);
            CompiledQuery query = takeQuery(
                    messageWrapper.getCollectionName().toString(),
                    messageWrapper.getIndexes(),
                    storageConnection);
            setInternalState(query);
        } catch (ResolutionException | ReadValueException | TaskSetConnectionException e) {
            throw new TaskPrepareException("'Create collection task' preparation has been failed because:", e);
        }
    }

    /**
     * {@see ITask} {@link ITask#execute()}
     * Executes a create collection compiledQuery to psql database.
     *
     * @throws TaskExecutionException {@see ITask} {@link ITask#execute()}
     */
    @Override
    public void execute() throws TaskExecutionException {
        if (compiledQuery == null) {
            throw new TaskExecutionException("Should first prepare the task.");
        }

        try {
            compiledQuery.execute();
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Create collection task' execution has been failed because: ", e);
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
    public void setStorageConnection(@NotNull final StorageConnection storageConnection)
            throws TaskSetConnectionException {

        verify(storageConnection);
        this.storageConnection = storageConnection;
    }

    private CompiledQuery takeQuery(
            final String collection,
            final Map<String, String> indexes,
            final StorageConnection connection
    ) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(CompiledQuery.class.toString()),
                connection,
                getQueryStatementFactory(collection, indexes));
    }

    private QueryStatementFactory getQueryStatementFactory(final String collection, final Map<String, String> indexes) {
        return () -> {
            try {
                return QueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .withIndexes(indexes)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize create collection compiledQuery.", e);
            }
        };
    }

    private ICreateCollectionQueryMessage takeQueryMessage(final IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ICreateCollectionQueryMessage.class.toString()),
                object);
    }

    private void setInternalState(final CompiledQuery query) {
        this.compiledQuery = query;
    }

    private void verify(final StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null) {
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        }
        if (connection.getId() == null || connection.getId().isEmpty()) {
            throw new TaskSetConnectionException("Connection should have an id!");
        }
    }
}
