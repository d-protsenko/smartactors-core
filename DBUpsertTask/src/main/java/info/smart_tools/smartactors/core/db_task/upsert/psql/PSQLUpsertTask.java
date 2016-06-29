package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.upsert.DBUpsertTask;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.IUpsertQueryMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskInitializationException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;
import info.smart_tools.smartactors.core.sql_commons.psql.Schema;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task for upsert row to collection:
 *          1. Executes update operation if incoming query contains id;
 *          2. Executes insert operation otherwise.
 */
public class PSQLUpsertTask implements DBUpsertTask {

    private static final String INSERT_MODE = "insert";
    private static final String UPDATE_MODE = "update";

    /**
     *
     *
     * @throws TaskInitializationException
     */
    protected PSQLUpsertTask() throws TaskInitializationException {

    }

    @Override
    public void prepare(final IObject upsertObject) throws TaskPrepareException {

        try {
            IUpsertQueryMessage IUpsertQueryMessage = IOC.resolve(Keys.getOrAdd(IUpsertQueryMessage.class.toString()), upsertObject);
            this.collectionName = IUpsertQueryMessage.getCollectionName();
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Error while resolving upsert message.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new TaskPrepareException("Error while get collection name.", e);
        }
        try {
            this.idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), collectionName + "Id");
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create idFieldName.", e);
        }

        this.rawUpsertQuery = upsertObject;
        try {
            String id = IOC.resolve(Keys.getOrAdd(String.class.toString()), upsertObject.getValue(idFieldName));
            if (id != null) {
                this.mode = UPDATE_MODE;
                QueryStatementFactory factory = () -> {
                    QueryStatement updateQueryStatement = new QueryStatement();
                    Writer writer = updateQueryStatement.getBodyWriter();
                    try {
                        writer.write(String.format(
                            "UPDATE %s AS tab SET %s = docs.document FROM (VALUES", CollectionName.fromString(collectionName).toString(), Schema.DOCUMENT_COLUMN_NAME
                        ));
                        writer.write("(?,?::jsonb)");
                        writer.write(String.format(") AS docs (id, document) WHERE tab.%s = docs.id;", Schema.ID_COLUMN_NAME));
                    } catch (IOException | QueryBuildException e) {
                        throw new QueryStatementFactoryException("Error while initialize update query.", e);
                    }
                    return updateQueryStatement;
                };
                this.compiledQuery = IOC.resolve(Keys.getOrAdd(CompiledQuery.class.toString()), connection, PSQLUpsertTask.class.toString().concat("update"), factory);

                List<SQLQueryParameterSetter> parameterSetters = new ArrayList<>();
                parameterSetters.add((statement, index) -> {
                    try {
                        statement.setLong(index++, Long.parseLong(id));
                        statement.setString(index++, upsertObject.toString());
                    } catch (NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while writing update query statement: ", e);
                    }
                    return index;
                });
                this.compiledQuery.setParameters(parameterSetters);
            } else {
                this.mode = INSERT_MODE;
            }
        } catch (ReadValueException | StorageException | ResolutionException e) {
            throw new TaskPrepareException("Error while writing update query statement.",e);
        }
    }

    public void setStorageConnection(final StorageConnection storageConnection) throws TaskSetConnectionException {
        this.connection = storageConnection;
    }

    @Override
    public void execute() throws TaskExecutionException {
        executionMap.get(mode).upsert();
    }
}
