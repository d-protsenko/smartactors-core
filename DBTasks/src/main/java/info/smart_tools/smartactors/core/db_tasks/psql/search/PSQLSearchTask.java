package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
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
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Task for searching documents in database.
 */
public class PSQLSearchTask extends DBSearchTask<ISearchMessage> {

    private PSQLSearchTask() {}

    /**
     * Factory method for creation new instance of {@link PSQLSearchTask}.
     *
     * @return
     */
    public static PSQLSearchTask create() {
       return new PSQLSearchTask();
    }

    @Nonnull
    @Override
    protected ISearchMessage takeMessageWrapper(@Nonnull final IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ISearchMessage.class.toString()),
                object);
    }

    @Override
    protected boolean requiresExit(@Nonnull final ISearchMessage message) throws InvalidArgumentException {
        return false;
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeQuery(@Nonnull final IStorageConnection connection,
                                       @Nonnull final ISearchMessage message
    ) throws QueryBuildException {
        try {
            ICachedQuery cachedSearchQuery = message
                    .getCachedQuery()
                    .orElse(createCachedSearchQuery(connection, message));
            message.setCachedQuery(cachedSearchQuery);

            return cachedSearchQuery.getCompiledQuery();
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected ICompiledQuery setParameters(@Nonnull final ICompiledQuery query,
                                           @Nonnull final ISearchMessage message
    ) throws QueryBuildException {
        query.setParameters(message
                .getCachedQuery()
                .orElseThrow(() -> new QueryBuildException("Prepare cached search query is null!"))
                .getParametersSetters());

        return query;
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery query,
                           @Nonnull final ISearchMessage message
    ) throws TaskExecutionException {
        try {
            message.setSearchResult(super.execute(query));
        } catch (ChangeValueException e) {
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

    private QueryStatementFactory getQueryStatementFactory(final ISearchMessage message,
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

