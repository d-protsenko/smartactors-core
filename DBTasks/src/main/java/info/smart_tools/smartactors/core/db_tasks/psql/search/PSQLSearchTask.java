package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.commons.DBSearchTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ICachedQuery;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Task for searching documents in database.
 */
public class PSQLSearchTask extends DBSearchTask {

    private PSQLSearchTask() {}

    /**
     * Factory method for creation new instance of {@link PSQLSearchTask}.
     *
     * @return
     */
    public static PSQLSearchTask create() {
       return new PSQLSearchTask();
    }

    @Override
    protected boolean requiresNonExecutable(@Nonnull final IObject queryMessage) throws InvalidArgumentException {
        return false;
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeCompiledQuery(@Nonnull final IStorageConnection connection,
                                               @Nonnull final IObject message
    ) throws QueryBuildException {
        try {

        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected ICompiledQuery setParameters(@Nonnull final ICompiledQuery query,
                                           @Nonnull final IObject message
    ) throws QueryBuildException {


        return query;
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery query,
                           @Nonnull final IObject message
    ) throws TaskExecutionException {
        try {
            DBQueryFields.SEARCH_RESULT.out(message, super.execute(query));
        } catch (ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

    private ICachedQuery createCachedSearchQuery(final IStorageConnection connection,
                                                 final ISearchMessage message
    ) throws ResolutionException, ReadValueException {
        List<ISQLQueryParameterSetter> setters = new ArrayList<>();
        ICompiledQuery compiledQuery = IOC.resolve(
                Keys.getOrAdd(ICompiledQuery.class.toString()),
                connection,
                getQueryStatementFactory(message, setters));

        return IOC.resolve(
                Keys.getOrAdd(ICachedQuery.class.toString()),
                compiledQuery,
                setters);
    }

    private IQueryStatementFactory getQueryStatementFactory(final ISearchMessage message,
                                                            final List<ISQLQueryParameterSetter> setters
    ) {
        return () -> {
            try {
                return SearchQueryStatementBuilder
                        .create()
                        .withCollection(message.getCollection().toString())
                        .withCriteria(message.getCriteria())
                        .withOrderByItems(message.getOrderBy())
                        .withPageNumber(message.getPageNumber())
                        .withPageSize(message.getPageSize())
                        .withSQLSetters(setters)
                        .build();
            } catch (QueryBuildException | ReadValueException e) {
                throw new QueryStatementFactoryException("Error while initialize a search by id query.", e);
            }
        };
    }
}

