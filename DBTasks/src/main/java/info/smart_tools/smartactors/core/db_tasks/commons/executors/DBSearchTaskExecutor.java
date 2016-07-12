package info.smart_tools.smartactors.core.db_tasks.commons.executors;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_tasks.commons.DBQueryFields;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Common a searching task executor.
 */
public class DBSearchTaskExecutor implements IDBTaskExecutor {
    /**
     * Default constructor.
     */
    private DBSearchTaskExecutor() {}

    public static DBSearchTaskExecutor create() {
        return new DBSearchTaskExecutor();
    }

    @Override
    public boolean requiresExecutable(@Nonnull final IObject message) throws InvalidArgumentException {
        return true;
    }

    /**
     * Executes a searching query of rows to database and pushes the result in the incoming message.
     *
     * @param query - a compiled executable query to database.
     *
     * @throws TaskExecutionException when :
     *                1. IOC resolution error;
     *                2. Error change a value into {@link IObject};
     *                3. Error during search query execution.
     */
    @Override
    public void execute(@Nonnull final ICompiledQuery query, @Nonnull final IObject message)
            throws TaskExecutionException {
        try {
            ResultSet resultSet = query.executeQuery();
            List<IObject> resultObjects = new LinkedList<>();
            while (resultSet.next()) {
                String jsonValue = resultSet.getString("document");
                IObject object;
                try {
                    object = IOC.resolve(Keys.getOrAdd(IObject.class.toString()), jsonValue);
                    IFieldName idFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "id");
                    object.setValue(idFN, resultSet.getLong("id"));
                } catch (ChangeValueException e) {
                    throw new TaskExecutionException("Could not set document's id field.", e);
                } catch (ResolutionException e) {
                    throw new TaskExecutionException(e.getMessage(), e);
                } catch (InvalidArgumentException e) {
                    throw new TaskExecutionException("Invalid argument exception", e);
                }

                resultObjects.add(object);
            }

            DBQueryFields.SEARCH_RESULT.out(message, resultObjects);
        } catch (QueryExecutionException | SQLException e) {
            throw new TaskExecutionException("'Search query' execution has been failed: " + e.getMessage(), e);
        } catch (InvalidArgumentException | ChangeValueException e) {
            throw new TaskExecutionException("Error writing search result: " + e.getMessage(), e);
        }
    }
}
