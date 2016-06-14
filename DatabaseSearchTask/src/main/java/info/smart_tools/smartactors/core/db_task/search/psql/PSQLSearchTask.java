package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.search.DBSearchTask;
import info.smart_tools.smartactors.core.db_task.search.utils.SearchQueryWriter;
import info.smart_tools.smartactors.core.db_task.search.utils.sql.GeneralSQLOrderWriter;
import info.smart_tools.smartactors.core.db_task.search.utils.sql.GeneralSQLPagingWriter;
import info.smart_tools.smartactors.core.db_task.search.wrappers.SearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryConditionWriterResolver;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;

/**
 * Task for searching documents in database.
 */
public class PSQLSearchTask extends DBSearchTask {
    /** Current connection to database. */
    private StorageConnection connection;
    /** Compiled searching query. */
    private CompiledQuery query;
    /** {@see SearchQuery} {@link SearchQuery} */
    private SearchQuery message;

    /** Modules for building a searching query. */
    private QueryConditionWriterResolver conditionsWriterResolver;
    private SearchQueryWriter orderWriter;
    private SearchQueryWriter pagingWriter;

    /**
     * A single constructor for creation {@link PSQLSearchTask}
     */
    private PSQLSearchTask(
            final QueryConditionWriterResolver conditionsWriterResolver,
            final SearchQueryWriter orderWriter,
            final SearchQueryWriter pagingWriter
    ) {
        this.conditionsWriterResolver = conditionsWriterResolver;
        this.orderWriter = orderWriter;
        this.pagingWriter = pagingWriter;
    }

    /**
     * Factory method for creation new instance of {@link PSQLSearchTask}.
     */
    public static PSQLSearchTask create() {
        return new PSQLSearchTask(
                ConditionsWriterResolver.create(),
                GeneralSQLOrderWriter.create(),
                GeneralSQLPagingWriter.create());
    }

    /**
     * {@see IDatabaseTask} {@link info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask}
     * Prepare query for searching documents in database by some criteria.
     *
     * @param prepareMessage {@see SearchQuery} {@link SearchQuery}
     *
     * @throws TaskPrepareException when:
     *                1. IOC resolution error;
     *                2. Error building a searching query statement.
     */
    @Override
    public void prepare(@Nonnull final IObject prepareMessage) throws TaskPrepareException {
        try {
            this.message = IOC.resolve(Keys.getOrAdd(SearchQuery.class.toString()), prepareMessage);
            query = createQuery(this.message);
        } catch (ResolutionException e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }
    }

    /**
     * {@see ITask} {@link info.smart_tools.smartactors.core.itask.ITask}
     * Executes searching documents by criteria from database.
     *
     * @throws TaskExecutionException when error in execution process.
     */
    @Override
    public void execute() throws TaskExecutionException {
        execute(query, message);
    }

    @Override
    public void setConnection(@Nonnull final StorageConnection storageConnection) throws TaskSetConnectionException {
        this.connection = storageConnection;
    }

    private CompiledQuery createQuery(final SearchQuery queryMessage) throws TaskPrepareException {
        QueryStatement queryStatement = new QueryStatement();
        try {
            queryStatement.getBodyWriter().write(String.format("SELECT * FROM %s WHERE",
                    CollectionName.fromString(queryMessage.getCollectionName()).toString()));

            conditionsWriterResolver
                    .resolve(null)
                    .write(queryStatement, conditionsWriterResolver, null, queryMessage.getCriteria());
            orderWriter.write(queryStatement, queryMessage);
            pagingWriter.write(queryStatement, queryMessage);

            return connection.compileQuery(queryStatement);
        } catch (QueryBuildException e) {
            throw new TaskPrepareException("Error while writing search query statement.", e);
        } catch (Exception e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }
    }
}
