package info.smart_tools.smartactors.core.db_tasks.psql.insert;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.QueryKey;
import info.smart_tools.smartactors.core.db_tasks.commons.DBInsertTask;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import javax.annotation.Nonnull;

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
    protected ICompiledQuery takeCompiledQuery(
            @Nonnull final IStorageConnection connection,
            @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IKey queryKey = QueryKey.create(
                    connection.getId(),
                    PSQLInsertTask.class.toString(),
                    collection);

            return takeCompiledQuery(
                    queryKey,
                    connection,
                    getQueryStatementFactory(collection));
        } catch (ReadValueException | InvalidArgumentException e) {
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
    protected ICompiledQuery setParameters(@Nonnull final ICompiledQuery query, @Nonnull final IObject message)
            throws QueryBuildException {
        try {
            String document = DBQueryFields.DOCUMENT.in(message);
            query.setParameters(statement -> {
                statement.setString(1, document);
            });

            return query;
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param collection
     * @return
     */
    private IQueryStatementFactory getQueryStatementFactory(final String collection) {
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
