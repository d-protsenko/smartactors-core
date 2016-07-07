package info.smart_tools.smartactors.core.db_tasks.psql.delete;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBDeleteTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.delete.IDeleteMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Collections;

/**
 * Task for deletion documents from database.
 */
public class PSQLDeleteTask extends DBDeleteTask {
    /**
     * A single constructor for creation {@link PSQLDeleteTask}
     *
     */
    protected PSQLDeleteTask() {}

    /**
     * Factory method for creation new instance of {@link PSQLDeleteTask}.
     */
    public static PSQLDeleteTask create() {
        return new PSQLDeleteTask();
    }

    @Nonnull
    @Override
    protected IDeleteMessage takeMessageWrapper(@Nonnull final IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IDeleteMessage.class.toString()),
                object);
    }

    @Nonnull
    @Override
    protected CompiledQuery takeQuery(@Nonnull final StorageConnection connection,
                                      @Nonnull final IDeleteMessage message
    ) throws QueryBuildException {
        try {
            String collection = message.getCollection().toString();
            IKey queryKey = IOC.resolve(
                    Keys.getOrAdd(QueryKey.class.toString()),
                    connection.getId(),
                    PSQLDeleteTask.class.toString(),
                    collection);

            return IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                    queryKey,
                    connection,
                    getQueryStatementFactory(collection));
        } catch (ResolutionException | ReadValueException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected CompiledQuery setParameters(@Nonnull final CompiledQuery query,
                                          @Nonnull final IDeleteMessage message
    ) throws QueryBuildException {
        query.setParameters(Collections.singletonList((statement, index) -> {
            statement.setLong(index++, message.getDocumentId());
            return index;
        }));

        return query;
    }

    private QueryStatementFactory getQueryStatementFactory(final String collection) {
        return () -> {
            try {
                return QueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize update query.", e);
            }
        };
    }
}