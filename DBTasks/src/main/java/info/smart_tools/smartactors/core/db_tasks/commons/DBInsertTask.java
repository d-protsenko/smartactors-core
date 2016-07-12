package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Common insert task executor.
 */
public abstract class DBInsertTask extends CachedDatabaseTask {

    protected DBInsertTask() {}

    @Override
    protected boolean requiresExecutable(@Nonnull IObject message) throws InvalidArgumentException {
        try {
            return DBQueryFields.DOCUMENT.in(message) != null;
        } catch (ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Executes insertion documents query to database.
     *
     * @param query - a compiled executable query to database.
     * @param message - source message with parameters for query.
     *
     * @throws TaskExecutionException when the result set has more than one document.
     */
    @Override
    protected void execute(@Nonnull final ICompiledQuery query, @Nonnull final IObject message)
            throws TaskExecutionException {
        try {
            ResultSet resultSet = query.executeQuery();
            if (resultSet == null || !resultSet.first()) {
                throw new TaskExecutionException("Query execution has been failed: " +
                        "Database returned not enough generated ids");
            }

            addIdsInResult(resultSet, message);
        } catch (QueryExecutionException | SQLException e) {
            throw new TaskExecutionException("'Insert query' execution has been failed: " + e.getMessage(), e);
        }
    }

    private void addIdsInResult(
            final ResultSet resultSet,
            final IObject message
    ) throws TaskExecutionException {
        try {
            int docCounter = 0;
            while (resultSet.next()) {
                try {
                    String collection = DBQueryFields.COLLECTION.in(message);
                    setDocumentId(DBQueryFields.DOCUMENT.in(message), collection, resultSet.getLong("id"));
                } catch (ChangeValueException e) {
                    throw new TaskExecutionException("Could not set new id on inserted document.");
                } catch (ReadValueException | InvalidArgumentException e) {
                    throw new TaskExecutionException("Could not read document with index " + docCounter + ".");
                }
                docCounter++;
            }
            if (docCounter >= 1) {
                throw new TaskExecutionException("Database returned too much of generated ids.");
            }
            if (docCounter < 1) {
                throw new TaskExecutionException("Database returned not enough of generated ids.");
            }
        } catch (SQLException | ResolutionException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

    private void setDocumentId(final IObject document, final String collection, final Long documentId)
            throws ChangeValueException, ResolutionException, InvalidArgumentException {
        IField id = getIdFieldFor(collection);
        id.out(document, documentId);
    }
}
