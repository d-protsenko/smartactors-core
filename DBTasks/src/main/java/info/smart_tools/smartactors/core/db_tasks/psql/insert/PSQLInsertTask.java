package info.smart_tools.smartactors.core.db_tasks.psql.insert;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBInsertTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.insert.IInsertMessage;
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
 * Task for insert documents in postgres database.
 */
public class PSQLInsertTask extends DBInsertTask {
    /**
     * Default constructor.
     *              Creates a new instance of {@link PSQLInsertTask}.
     */
    protected PSQLInsertTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLInsertTask}.
     *
     * @return a new instance of {@link PSQLInsertTask}.
     */
    public static PSQLInsertTask create() {
        return new PSQLInsertTask();
    }

    @Nonnull
    @Override
    protected IInsertMessage takeMessageWrapper(@Nonnull IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IInsertMessage.class.toString()),
                object);
    }

    @Nonnull
    @Override
    protected ICompiledQuery takeQuery(
            @Nonnull final IStorageConnection connection,
            @Nonnull final IInsertMessage queryMessage
    ) throws QueryBuildException {
        try {
            String collection = queryMessage.getCollection().toString();
            IKey queryKey = IOC.resolve(
                    Keys.getOrAdd(QueryKey.class.toString()),
                    connection.getId(),
                    PSQLInsertTask.class.toString(),
                    collection);

            return IOC.resolve(
                    Keys.getOrAdd(ICompiledQuery.class.toString() + "USED_CACHE"),
                    queryKey,
                    connection,
                    getQueryStatementFactory(collection));
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param query
     * @param message
     * @return
     * @throws QueryBuildException
     */
    @Nonnull
    @Override
    protected ICompiledQuery setParameters(final ICompiledQuery query, final IInsertMessage message)
            throws QueryBuildException {
        try {
            String document = message.getDocument().toString();
            query.setParameters(Collections.singletonList((statement, index) -> {
                statement.setString(index++, document);
                return index;
            }));

            return query;
        } catch (ReadValueException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }



    /**
     *
     * @param collection
     * @return
     */
    private QueryStatementFactory getQueryStatementFactory(final String collection) {
        return  () -> {
            try {
                return QueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize an insert query.", e);
            }
        };
    }
}
