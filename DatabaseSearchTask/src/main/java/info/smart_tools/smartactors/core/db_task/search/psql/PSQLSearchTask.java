package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.search.DBSearchTask;
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
import java.io.IOException;

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

    /**  */
    private static final int MAX_PAGE_SIZE = 10000;
    private static final int MIN_PAGE_SIZE = 1;

    private QueryConditionWriterResolver conditionsWriterResolver;
    private OrderWriter orderWriter;

    /**
     * A single constructor for creation {@link PSQLSearchTask}
     */
    private PSQLSearchTask() {
        conditionsWriterResolver = new ConditionsWriterResolver();
    }

    /**
     * Factory method for creation new instance of {@link PSQLSearchTask}.
     */
    public static PSQLSearchTask create() {
        return new PSQLSearchTask();
    }

    /**
     * {@see IDatabaseTask} {@link info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask}
     * Prepare query for searching documents in database by some criteria.
     *
     * @param message
     *
     * @throws TaskPrepareException when:
     *
     */
    @Override
    public void prepare(@Nonnull final IObject message) throws TaskPrepareException {
        try {
            this.message = IOC.resolve(Keys.getOrAdd(SearchQuery.class.toString()), message);
            query = createQuery(this.message);
        } catch (ResolutionException e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        execute(query, message);
    }

    @Override
    public void setConnection(@Nonnull final StorageConnection storageConnection) throws TaskSetConnectionException {
        this.connection = storageConnection;
    }

    private CompiledQuery createQuery(final SearchQuery searchMessage) throws TaskPrepareException {
        QueryStatement queryStatement = new QueryStatement();
        try {
            queryStatement.getBodyWriter().write(String.format("SELECT * FROM %s WHERE",
                    CollectionName.fromString(searchMessage.getCollectionName()).toString()));

            conditionsWriterResolver
                    .resolve(null)
                    .write(queryStatement, conditionsWriterResolver, null, searchMessage.getCriteria());

            orderWriter.writeOrderByStatement(queryStatement, searchMessage);

            queryStatement.getBodyWriter().write("LIMIT(?)OFFSET(?)");
            queryStatement.pushParameterSetter((statement, index) -> {
                int pageSize = searchMessage.getPageSize();
                int pageNumber = searchMessage.getPageNumber() - 1;

                pageNumber = (pageNumber < 0) ? 0 : pageNumber;
                pageSize = (pageSize > MAX_PAGE_SIZE) ?
                        MAX_PAGE_SIZE : ((pageSize < MIN_PAGE_SIZE) ? MIN_PAGE_SIZE : pageSize);

                statement.setInt(index++, pageSize);
                statement.setInt(index++, pageSize * pageNumber);
                return index;
            });

            return connection.compileQuery(queryStatement);
        } catch (IOException e) {
            throw new TaskPrepareException("Error while writing search query statement.",e);
        } catch (Exception e) {
            throw new TaskPrepareException(e.getMessage(), e);
        }
    }
}
