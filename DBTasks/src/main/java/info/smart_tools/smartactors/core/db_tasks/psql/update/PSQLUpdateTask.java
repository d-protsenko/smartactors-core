package info.smart_tools.smartactors.core.db_tasks.psql.update;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.db_tasks.commons.DBUpdateTask;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.sql_commons.QueryKey;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;
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
    private PSQLUpdateTask() { }

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
    protected ICompiledQuery takeCompiledQuery(
            @Nonnull final IStorageConnection connection,
            @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IKey queryKey = QueryKey.create(
                    connection.getId(),
                    PSQLUpdateTask.class.toString(),
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
    protected ICompiledQuery setParameters(
            @Nonnull final ICompiledQuery compiledQuery,
            @Nonnull final IObject message
    ) throws QueryBuildException {
        try {
            String collection = DBQueryFields.COLLECTION.in(message);
            IObject document = DBQueryFields.DOCUMENT.in(message);
            String documentId = takeDocumentId(document, collection);
            String documentJson = document.serialize();

            compiledQuery.setParameters(Collections.singletonList((statement, index) -> {
                statement.setString(index++, documentId);
                statement.setString(index++, documentJson);

                return index;
            }));
        } catch (ReadValueException | InvalidArgumentException |
                SerializeException | ResolutionException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }

        return compiledQuery;
    }

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

    private String takeDocumentId(final IObject document, final String collection)
            throws ResolutionException, ReadValueException, InvalidArgumentException {
        IField id = getIdFieldFor(collection);
        return id.in(document);
    }
}
