package info.smart_tools.smartactors.core.db_tasks.psql.create_collection;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBCreateCollectionTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.create_collection.ICreateCollectionMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Task for create collection with predefined indexes in psql database.
 */
public class PSQLCreateCollectionTask extends DBCreateCollectionTask {
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

    @Nonnull
    @Override
    protected ICreateCollectionMessage takeMessageWrapper(@Nonnull IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ICreateCollectionMessage.class.toString()),
                object);
    }

    @Nonnull
    @Override
    protected CompiledQuery takeQuery(
            final StorageConnection connection,
            final ICreateCollectionMessage message
    ) throws QueryBuildException {
        try {
            return IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString()),
                    connection,
                    getQueryStatementFactory(
                            message.getCollection().toString(),
                            message.getIndexes()));
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected CompiledQuery setParameters(@Nonnull final CompiledQuery query,
                                          @Nonnull final ICreateCollectionMessage message
    ) throws QueryBuildException {
        return query;
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
}
