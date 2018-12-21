package info.smart_tools.smartactors.database_postgresql.postgres_connection;

import info.smart_tools.smartactors.class_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

class TestDriver implements Driver {

    private Connection connection = mock(Connection.class);

    @Override
    public Connection connect(String s, Properties properties) throws SQLException {
        return connection;
    }

    @Override
    public boolean acceptsURL(String s) throws SQLException {
        return false;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}

@PrepareForTest({PostgresConnection.class})
@RunWith(PowerMockRunner.class)
public class PostgresConnectionTest {

    private Connection connection;
    private PostgresConnection testPostgresConnection;
    private PreparedStatement validateStatement;

    @Before
    public void before() throws Exception {
        IModule module = mock(IModule.class);
        ISmartactorsClassLoader classLoader = mock(ISmartactorsClassLoader.class);
        ModuleManager.setCurrentModule(module);
        when(module.getClassLoader()).thenReturn(classLoader);
        when(classLoader.loadClass(any())).thenReturn((Class) TestDriver.class);

        ConnectionOptions connectionOptions = mock(ConnectionOptions.class);
        String url = "asd";
        String name = "asdsad";
        String password = "asdasdasd";
        when(connectionOptions.getUrl()).thenReturn(url);
        when(connectionOptions.getUsername()).thenReturn(name);
        when(connectionOptions.getPassword()).thenReturn(password);

        Statement statement = mock(Statement.class);
        testPostgresConnection = new PostgresConnection(connectionOptions);
        Field con = testPostgresConnection.getClass().getDeclaredField("connection");
        con.setAccessible(true);
        connection = (Connection) con.get(testPostgresConnection);
        validateStatement = mock(PreparedStatement.class);
        Field vqs = testPostgresConnection.getClass().getDeclaredField("validationQueryStatement");
        vqs.setAccessible(true);
        vqs.set(testPostgresConnection, validateStatement);

        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement("SELECT(1);")).thenReturn(validateStatement);

        verify(connection).setAutoCommit(false);
        verify(connection).prepareStatement("SELECT(1);");
    }

    @Test
    public void MustCorrectValidate() throws SQLException, StorageException {
        when(connection.isClosed()).thenReturn(false);

        assertTrue(testPostgresConnection.validate());

        verify(connection).isClosed();
        verify(validateStatement).executeQuery();
    }

    @Test
    public void MustCorrectValidateWhenFirstExecuteThrowException() throws SQLException, StorageException {
        when(connection.isClosed()).thenReturn(false);

        when(validateStatement.executeQuery()).thenThrow(new SQLException()).thenReturn(null);

        assertTrue(testPostgresConnection.validate());

        verify(connection).isClosed();
        verify(validateStatement, times(2)).executeQuery();
        verify(connection).rollback();
    }

    @Test
    public void MustCorrectValidateWhenIsClosedTrue() throws SQLException, StorageException {
        when(connection.isClosed()).thenReturn(true);

        assertTrue(!testPostgresConnection.validate());

        verify(connection).isClosed();
    }

    @Test
    public void MustInCorrectValidateWhenExecuteTwiceThrowException() throws SQLException, StorageException {
        when(connection.isClosed()).thenReturn(false);

        when(validateStatement.executeQuery()).thenThrow(new SQLException());

        try {
            testPostgresConnection.validate();
        } catch (IllegalArgumentException e) {
            verify(connection).isClosed();
            verify(validateStatement, times(2)).executeQuery();
            verify(connection).rollback();
            return;
        }
        fail("Must throw exception");
    }

    @Test
    public void MustInCorrectValidateWhenIsClosedThrowException() throws SQLException, StorageException {
        when(connection.isClosed()).thenThrow(new SQLException());

        try {
            testPostgresConnection.validate();
        } catch (StorageException e) {
            verify(connection).isClosed();
            return;
        }
        fail("Must throw exception");
    }

    @Test
    public void MustCorrectCloseConnection() throws StorageException, SQLException {
        testPostgresConnection.close();
        verify(connection).close();
    }

    @Test
    public void MustInCorrectCloseConnectionWhenCloseThrowException() throws StorageException, SQLException {
        doThrow(new SQLException()).when(connection).close();

        try {
            testPostgresConnection.close();
        } catch (StorageException e) {
            verify(connection).close();
            return;
        }
        fail("Must throw exception");
    }

    @Test
    public void MustCorrectCommitConnection() throws StorageException, SQLException {
        testPostgresConnection.commit();
        verify(connection).commit();
    }

    @Test
    public void MustInCorrectCommitConnectionWhenCommitThrowException() throws StorageException, SQLException {
        doThrow(new SQLException()).when(connection).commit();

        try {
            testPostgresConnection.commit();
        } catch (StorageException e) {
            verify(connection).commit();
            return;
        }
        fail("Must throw exception");
    }

    @Test
    public void MustCorrectCommitRollback() throws StorageException, SQLException {
        testPostgresConnection.rollback();
        verify(connection).rollback();
    }

    @Test
    public void MustInCorrectRollbackConnectionWhenRollbackThrowException() throws StorageException, SQLException {
        doThrow(new SQLException()).when(connection).rollback();

        try {
            testPostgresConnection.rollback();
        } catch (StorageException e) {
            verify(connection).rollback();
            return;
        }
        fail("Must throw exception");
    }

    @Test
    public void MustCorrectCompileQuery() throws Exception {
        QueryStatement statement = mock(QueryStatement.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        JDBCCompiledQuery query = mock(JDBCCompiledQuery.class);

        when(statement.compile(connection)).thenReturn(preparedStatement);
        whenNew(JDBCCompiledQuery.class).withArguments(preparedStatement).thenReturn(query);

        assertEquals(testPostgresConnection.compileQuery(statement), query);

        verify(statement).compile(connection);
        verifyNew(JDBCCompiledQuery.class).withArguments(preparedStatement);
    }

    @Test
    public void MustInCorrectCompileQueryWhenCompileThrowException() throws Exception {
        QueryStatement statement = mock(QueryStatement.class);

        Writer bodyWriter = mock(Writer.class);
        when(bodyWriter.toString()).thenReturn("as");
        when(statement.getBodyWriter()).thenReturn(bodyWriter);

        when(statement.compile(connection)).thenThrow(new SQLException());

        try {
            testPostgresConnection.compileQuery(statement);
        } catch (StorageException e) {
            verify(statement).compile(connection);
            return;
        }
        fail("Must throw exception");
    }

}