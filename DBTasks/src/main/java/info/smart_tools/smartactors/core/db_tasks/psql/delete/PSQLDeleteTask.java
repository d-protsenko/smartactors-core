package info.smart_tools.smartactors.core.db_tasks.psql.delete;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBDeleteTask;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.psql.insert.PSQLInsertTask;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
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
    protected ICompiledQuery takeCompiledQuery(@Nonnull final IStorageConnection connection,
                                               @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IKey queryKey = QueryKey.create(
                    connection.getId(),
                    PSQLDeleteTask.class.toString(),
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
            Long documentId = DBQueryFields.DOCUMENT_ID.in(message);
            query.setParameters(Collections.singletonList((statement, index) -> {
                statement.setLong(index++, documentId);
                return index;
            }));

            return query;
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    private IQueryStatementFactory getQueryStatementFactory(final String collection) {
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