package info.smart_tools.smartactors.core.db_tasks.psql.update;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBUpdateTask;
import info.smart_tools.smartactors.core.db_tasks.wrappers.update.IUpdateMessage;
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

    @Nonnull
    @Override
    protected IUpdateMessage takeMessageWrapper(@Nonnull IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(IUpdateMessage.class.toString()),
                object);
    }

    @Nonnull
    @Override
    protected CompiledQuery takeQuery(
            @Nonnull final StorageConnection connection,
            @Nonnull final IUpdateMessage queryMessage
    ) throws QueryBuildException {
        try {
            IKey queryKey = IOC.resolve(
                    Keys.getOrAdd(QueryKey.class.toString()),
                    connection.getId(),
                    PSQLUpdateTask.class.toString(),
                    queryMessage.getCollection().toString());

            return IOC.resolve(
                    Keys.getOrAdd(CompiledQuery.class.toString() + "USED_CACHE"),
                    queryKey,
                    connection,
                    getQueryStatementFactory(
                            queryMessage.getCollection().toString()));
        } catch (ReadValueException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    @Nonnull
    @Override
    protected CompiledQuery setParameters(
            @Nonnull final CompiledQuery compiledQuery,
            @Nonnull final IUpdateMessage updateQueryMessage
    ) throws QueryBuildException {
        compiledQuery.setParameters(Collections.singletonList((statement, index) -> {
            try {
                String collection = updateQueryMessage.getCollection().toString();
                String documentId = takeDocumentId(updateQueryMessage.getDocument(), collection);
                statement.setString(index++, documentId);
                statement.setString(index++, updateQueryMessage.getDocument().toString());

                return index;
            } catch (ReadValueException | ResolutionException e) {
                throw new QueryBuildException("Error while writing update query statement: " +
                        "could not read document's id.", e);
            }
        }));

        return compiledQuery;
    }

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

    private String takeDocumentId(final IObject document, final String collection)
            throws ResolutionException, ReadValueException {
        IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collection + "Id");
        return document.getValue(idFN).toString();

    }
}
