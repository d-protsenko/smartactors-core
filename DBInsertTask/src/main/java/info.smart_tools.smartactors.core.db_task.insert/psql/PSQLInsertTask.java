package info.smart_tools.smartactors.core.db_task.insert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.insert.DBInsertTask;
import info.smart_tools.smartactors.core.db_task.insert.psql.wrapper.IInsertMessage;
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
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;
import info.smart_tools.smartactors.core.sql_commons.psql.Schema;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class PSQLInsertTask extends DBInsertTask {
    private CompiledQuery query;
    private StorageConnection connection;

    /**
     *
     */
    private PSQLInsertTask() {}

    /**
     * Factory method for creation a new instance of {@link PSQLInsertTask}.
     *
     * @return a new instance of {@link PSQLInsertTask}.
     */
    public static PSQLInsertTask create() {
        return new PSQLInsertTask();
    }

    /**
     *
     * @param insertMessage
     * @throws TaskPrepareException
     */
    @Override
    public void prepare(final IObject insertMessage) throws TaskPrepareException {
        try {
            verify(connection);
            IInsertMessage messageWrapper = IOC.resolve(Keys.getOrAdd(IInsertMessage.class.getName()), insertMessage);
            if (messageWrapper.getDocument().getId() == null) {
                QueryStatementFactory factory = getQueryStatementFactory();
                this.query = IOC.resolve(
                        Keys.getOrAdd(CompiledQuery.class.toString()),
                        connection,
                        PSQLInsertTask.class.toString(), factory);

                List<SQLQueryParameterSetter> parameterSetters = new ArrayList<>();
                parameterSetters.add((statement, index) -> {
                    try {
                        statement.setString(index++ , message.toString());
                    } catch (NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while write insert query statement: ", e);
                    }
                    return index;
                });
                query.setParameters(parameterSetters);
            } else {
                return;
            }
        } catch (ChangeValueException | ReadValueException | ResolutionException |
                StorageException | TaskSetConnectionException e) {
            throw new TaskPrepareException("Can't prepare insert query");
        } catch (SQLException e){
            //TODO::
        }
    }

    /**
     *
     * @param connection
     * @throws TaskSetConnectionException
     */
    @Override
    public void setConnection(StorageConnection connection) throws TaskSetConnectionException {
        verify(connection);
        this.connection = connection;
    }

    /**
     *
     * @throws TaskExecutionException
     */
    @Override
    public void execute() throws TaskExecutionException {
        if (query == null)
            throw new TaskExecutionException("Should first prepare the task.");

        super.execute(query);
    }

    private CompiledQuery takeQuery(String collection, Map<String, String> indexes, StorageConnection connection)
            throws BuildingException, StorageException {
        QueryStatement queryStatement = QueryStatementBuilder
                .create()
                .withCollection(collection)
                .withIndexes(indexes)
                .build();

        return connection.compileQuery(queryStatement);
    }

    private ICreateCollectionQuery takeQueryMessage(IObject object) throws ResolutionException {
        return IOC.resolve(
                Keys.getOrAdd(ICreateCollectionQuery.class.toString()),
                object);
    }

    private void setInternalState(CompiledQuery query) {
        this.query = query;
    }

    private QueryStatementFactory getQueryStatementFactory() {
        return  () -> {
            QueryStatement updateQueryStatement = new QueryStatement();
            Writer writer = updateQueryStatement.getBodyWriter();
            try {
                writer.write(String.format("INSERT %s AS tab SET %s = docs.document FROM (VALUES",
                        CollectionName.fromString(CollectionName.fromString(collectionName).toString()),
                        Schema.DOCUMENT_COLUMN_NAME));
                writer.write("(?::jsonb)");
                writer.write(String.format(" RETURNING %s AS id;", Schema.ID_COLUMN_NAME));
            } catch (IOException | QueryBuildException e) {
                throw new QueryStatementFactoryException("Error while initialize update query.", e);
            }
            return updateQueryStatement;
        };
    }

    private void verify(StorageConnection connection) throws TaskSetConnectionException {
        if (connection == null)
            throw new TaskSetConnectionException("Connection should not be a null or empty!");
        if (connection.getId() == null || connection.getId().isEmpty())
            throw new TaskSetConnectionException("Connection should have an id!");
    }
}
