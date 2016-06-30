package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.upsert.DBUpdateTask;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IFieldName;
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
 * Task for update documents in postgres database.
 */
public class PSQLUpdateTask extends DBUpdateTask {
    /**
     * Default constructor.
     *              Creates a new instance of {@link PSQLUpdateTask}.
     */
    protected PSQLUpdateTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLUpdateTask}.
     *
     * @return a new instance of {@link PSQLUpdateTask}.
     */
    public static PSQLUpdateTask create() {
        return new PSQLUpdateTask();
    }

    @Override
    protected CompiledQuery takeQuery(
            @Nonnull final StorageConnection connection,
            @Nonnull final IUpsertQueryMessage queryMessage
    ) throws QueryBuildException {
        try {
            IKey queryKey = IOC.resolve(
                    Keys.getOrAdd(QueryKey.class.toString()),
                    connection.getId(),
                    PSQLUpdateTask.class.toString(),
                    queryMessage.getCollectionName().toString());

            return IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                    queryKey,
                    connection,
                    getQueryStatementFactory(
                            queryMessage.getCollectionName().toString()));
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Override
    protected CompiledQuery formatQuery(
            @Nonnull final CompiledQuery compiledQuery,
            @Nonnull final IUpsertQueryMessage updateQueryMessage
    ) throws QueryBuildException {
        compiledQuery.setParameters(Collections.singletonList((statement, index) -> {
            try {
                String collection = updateQueryMessage.getCollectionName().toString();
                String documentId = takeDocumentId(updateQueryMessage.getDocument(), collection);
                statement.setString(index++, documentId);
                statement.setString(index++, updateQueryMessage.getDocument().toString());

                return index;
            } catch (ReadValueException e) {
                throw new QueryBuildException("Error while writing update query statement: " +
                        "could not read document's id.", e);
            }
        }));

        return compiledQuery;
    }

    private QueryStatementFactory getQueryStatementFactory(final String collection) {
        return  () -> {
            try {
                return UpdateQueryStatementBuilder
                        .create()
                        .withCollection(collection)
                        .build();
            } catch (QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize an insert query.", e);
            }
        };
    }

    private String takeDocumentId(final IObject document, String collection)
            throws ResolutionException, ReadValueException {
        IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collection + "Id");
        return document.getValue(idFN).toString();
    }
}
