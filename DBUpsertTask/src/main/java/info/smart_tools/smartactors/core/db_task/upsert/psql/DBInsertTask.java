package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.UpsertMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.psql.Schema;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//TODO:: remove this stub!
public class DBInsertTask implements IDatabaseTask {

    private CompiledQuery compiledQuery;
    private QueryStatement queryStatement;
    private StorageConnection connection;

    public DBInsertTask() {
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {

        //TODO:: replace by InsertMessage iobject wrapper
        try {
            UpsertMessage upsertMessage = IOC.resolve(Keys.getOrAdd(UpsertMessage.class.toString()), query);
            String collectionName = upsertMessage.getCollectionName();
            Writer writer = queryStatement.getBodyWriter();
            writer.write(String.format(
                "INSERT INTO %s (%s) VALUES", CollectionName.fromString(collectionName).toString(), Schema.DOCUMENT_COLUMN_NAME
            ));
            writer.write("(?::jsonb)");
            writer.write(String.format(" RETURNING %s AS id;", Schema.ID_COLUMN_NAME));

//            queryStatement.pushParameterSetter((statement, index) -> {
//                statement.setString(index++, query.toString());
//                return index;
//            });
            this.compiledQuery = connection.compileQuery(queryStatement);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Error while resolving insert query statement.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new TaskPrepareException("Error while get collection name.", e);
        } catch (IOException | QueryBuildException e) {
            throw new TaskPrepareException("Error while initialize insert query.", e);
        } catch (StorageException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        this.connection = connection;
    }

    @Override
    public void execute() throws TaskExecutionException {

    }

    public CompiledQuery getCompiledQuery() {
        return compiledQuery;
    }

    public static void main(String[] args1) {
        String JDBC_DRIVER = "org.postgresql.Driver";
        String DB_URL = "jdbc:postgresql://localhost:5432/vp";

        //  Database credentials
        String USER = "test_user";
        String PASS = "qwerty";

        Connection conn = null;
        PreparedStatement stmt = null;

        try{
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.prepareStatement("SELECT * FROM service");
//            Statement stmt2 = conn.createStatement();
            ResultSet rs = stmt.executeQuery();

            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                int id  = rs.getInt("id");
//                int age = rs.getInt("age");
//                String first = rs.getString("first");
//                String last = rs.getString("last");

                //Display values
                System.out.println("ID: " + id);
//                System.out.print(", Age: " + age);
//                System.out.print(", First: " + first);
//                System.out.println(", Last: " + last);
            }
            //STEP 6: Clean-up environment
            rs.close();
//            stmt.close();
            conn.close();




            Connection connection1 = DriverManager.getConnection(DB_URL,USER,PASS);
            ResultSet rs2 = stmt.executeQuery();
            while(rs2.next()){
                //Retrieve by column name
                int id  = rs2.getInt("id");
//                int age = rs.getInt("age");
//                String first = rs.getString("first");
//                String last = rs.getString("last");

                //Display values
                System.out.println("ID: " + id);
//                System.out.print(", Age: " + age);
//                System.out.print(", First: " + first);
//                System.out.println(", Last: " + last);
            }
            //STEP 6: Clean-up environment
            rs2.close();
            stmt.close();
            connection1.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!");


        try {
            IKey<CompiledQuery> compiledQueryKey = Keys.getOrAdd(CompiledQuery.class.toString());
            Map<String, CompiledQuery> queryMap = new HashMap<>();
            try {
                IOC.register(compiledQueryKey, new CreateNewInstanceStrategy(
                    (args) -> {
                        StorageConnection connection = (StorageConnection) args[0];
                        String id = connection.getId();
                        CompiledQuery query = queryMap.get(id);
                        if (query != null) {
                            return query;
                        }
                        QueryStatement queryStatement = (QueryStatement) args[1];
                        try {
                            query = connection.compileQuery(queryStatement);
                            queryMap.put(id, query);
                            return query;
                            //TODO:: how to remove old queries from map???
                        } catch (StorageException e) {
                            throw new RuntimeException("Can't register ioc rule for compiled query", e);
                        }
                    }));
            } catch (RegistrationException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        } catch (ResolutionException e) {
            e.printStackTrace();
        }
    }
}
