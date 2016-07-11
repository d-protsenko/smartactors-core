package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.commons.DBSearchTask;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.db_storage.utils.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Database task for search documents by id
 */
public class PSQLSearchByIdTask extends DBSearchTask {
    /**
     * Constructor for DBGetByIdTask
     */
    protected PSQLSearchByIdTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLSearchByIdTask}.
     *
     * @return a new instance of {@link PSQLSearchByIdTask}.
     */
    public static PSQLSearchByIdTask create() {
        return new PSQLSearchByIdTask();
    }

    @Override
    protected boolean requiresNonExecutable(@Nonnull final IObject message) throws InvalidArgumentException {
        try {
            return DBQueryFields.DOCUMENT_ID.in(message) == null;
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeCompiledQuery(@Nonnull final IStorageConnection connection,
                                               @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IKey queryKey = QueryKey.create(
                    connection.getId(),
                    PSQLSearchByIdTask.class.toString(),
                    collection);

            return takeCompiledQuery(
                    queryKey,
                    connection,
                    getQueryStatementFactory(collection));
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected ICompiledQuery setParameters(@Nonnull final ICompiledQuery query,
                                           @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String id = DBQueryFields.DOCUMENT_ID.in(message);
            query.setParameters((statement) -> {
                statement.setObject(1, id);
            });

            return query;
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery query,
                           @Nonnull final IObject message
    ) throws TaskExecutionException {
        List<IObject> result = super.execute(query);
        if (result.size() > 1) {
            throw new TaskExecutionException("'Search query' execution has been failed: " +
                    "the given id correspond to multiple users!");
        }
        try {
            DBQueryFields.SEARCH_RESULT.out(message, result.get(0));
        } catch (ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException("'Search query' execution has been failed: " + e.getMessage(), e);
        }
    }

    private IQueryStatementFactory getQueryStatementFactory(final String collection) {
        return () -> {
            try {
                return SearchByIdQueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize a search by id query.", e);
            }
        };
    }
}