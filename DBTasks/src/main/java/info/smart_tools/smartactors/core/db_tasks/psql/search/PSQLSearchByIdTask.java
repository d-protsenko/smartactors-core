package info.smart_tools.smartactors.core.db_tasks.psql.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBSearchTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.search.ISearchByIdMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Database task for search documents by id
 */
public class PSQLSearchByIdTask extends DBSearchTask<ISearchByIdMessage> {
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

    @Nonnull
    @Override
    protected ISearchByIdMessage takeMessageWrapper(@Nonnull final IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ISearchByIdMessage.class.toString()),
                object);
    }

    @Override
    protected boolean requiresExit(@Nonnull final ISearchByIdMessage message) throws InvalidArgumentException {
        try {
            return message.getId() == null;
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected CompiledQuery takeQuery(@Nonnull final StorageConnection connection,
                                      @Nonnull final ISearchByIdMessage message
    ) throws QueryBuildException {
        try {
            String collection = message.getCollection().toString();
            IKey queryKey = IOC.resolve(
                    Keys.getOrAdd(QueryKey.class.toString()),
                    connection.getId(),
                    PSQLSearchByIdTask.class.toString(),
                    collection);

            return IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                    queryKey,
                    connection,
                    getQueryStatementFactory(collection));
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected CompiledQuery setParameters(@Nonnull final CompiledQuery query,
                                          @Nonnull final ISearchByIdMessage message
    ) throws QueryBuildException {
        try {
            String id = message.getId();
            query.setParameters(Collections.singletonList((statement, index) -> {
                statement.setObject(index++, id);
                return index;
            }));

            return query;
        } catch (ReadValueException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Override
    protected void execute(@Nonnull final CompiledQuery query,
                           @Nonnull final ISearchByIdMessage message
    ) throws TaskExecutionException {
        List<IObject> result = super.execute(query);
        if (result.size() > 1) {
            throw new TaskExecutionException("'Search query' execution has been failed: " +
                    "the given id correspond to multiple users!");
        }
        try {
            message.setSearchResult(result.get(0));
        } catch (ChangeValueException e) {
            throw new TaskExecutionException("'Search query' execution has been failed: " + e.getMessage(), e);
        }
    }

    private QueryStatementFactory getQueryStatementFactory(final String collection) {
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