package info.smart_tools.smartactors.core.db_task.search;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_task.search.wrappers.SearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class DBSearchTask implements IDatabaseTask {
    /**
     *
     */
    protected DBSearchTask() {}

    /**
     *
     * @param query
     * @param message
     * @throws TaskExecutionException
     */
    protected void execute(@Nonnull final CompiledQuery query, @Nonnull final SearchQuery message)
            throws TaskExecutionException {

        try {
            ResultSet resultSet = ((JDBCCompiledQuery) query).getPreparedStatement().executeQuery();
            List<IObject> objects = new LinkedList<>();

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
                }

                objects.add(object);
            }

            message.setSearchResult(objects);
        } catch (SQLException e) {
            throw new TaskExecutionException("Search query execution failed because of SQL exception.", e);
        }
    }
}
