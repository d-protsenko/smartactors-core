package info.smart_tools.smartactors.core.db_tasks.psql.update;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.ICollectionName;
import info.smart_tools.smartactors.core.db_storage.utils.QueryKey;
import info.smart_tools.smartactors.core.db_tasks.commons.CachedDatabaseTask;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.commons.executors.DBUpdateTaskExecutor;
import info.smart_tools.smartactors.core.db_tasks.commons.executors.IDBTaskExecutor;
import info.smart_tools.smartactors.core.db_tasks.commons.queries.IQueryStatementBuilder;
import info.smart_tools.smartactors.core.db_tasks.utils.IDContainer;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 * Task for update documents in postgres database.
 */
public class PSQLUpdateTask extends CachedDatabaseTask {

    private final QueryStatementBuilder queryStatementBuilder;
    private final IDBTaskExecutor taskExecutor;

    /**
     * Default constructor.
     *              Creates a new instance of {@link PSQLUpdateTask}.
     */
    private PSQLUpdateTask() {
        queryStatementBuilder = QueryStatementBuilder.create();
        taskExecutor = DBUpdateTaskExecutor.create();
    }

    /**
     * Factory method for creation a new instance of {@link PSQLUpdateTask}.
     *
     * @return a new instance of {@link PSQLUpdateTask}.
     */
    public static PSQLUpdateTask create() {
        return new PSQLUpdateTask();
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeCompiledQuery(
            @Nonnull final IStorageConnection connection,
            @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            ICollectionName collection = DBQueryFields.COLLECTION.in(message);
            IKey queryKey = QueryKey.create(
                    connection.getId(),
                    PSQLUpdateTask.class.toString(),
                    collection.toString());

            return takeCompiledQuery(
                    queryKey,
                    connection,
                    getQueryStatementBuilder(collection.toString()));
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected ICompiledQuery setParameters(
            @Nonnull final ICompiledQuery compiledQuery,
            @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IObject document = DBQueryFields.DOCUMENT.in(message);
            Long documentId = takeDocumentId(document, collection);
            String documentJson = document.serialize();

            compiledQuery.setParameters((statement) -> {
                statement.setLong(1, documentId);
                statement.setString(2, documentJson);
            });
        } catch (ReadValueException | InvalidArgumentException |
                SerializeException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }

        return compiledQuery;
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery query,
                           @Nonnull final IObject message
    ) throws TaskExecutionException {
        taskExecutor.execute(query, message);
    }

    @Override
    protected boolean requiresExecutable(@Nonnull final IObject message) throws InvalidArgumentException {
        return taskExecutor.isExecutable(message);
    }

    private IQueryStatementBuilder getQueryStatementBuilder(final String collection) {
        return queryStatementBuilder.withCollection(collection);
    }

    private Long takeDocumentId(final IObject document, final String collection)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        IField id = IDContainer.getIdFieldFor(collection);
        return Long.parseLong(id.in(document));
    }
}
