package info.smart_tools.smartactors.database_postgresql.postgres_connection;

import info.smart_tools.smartactors.class_management.module_manager.ModuleManager;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.ICompiledQuery;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IPreparedQuery;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Implementation of {@link IStorageConnection}
 */
public class PostgresConnection implements IStorageConnection {
    private static final String POSTGRESQL_JDBC_DRIVER_NAME = "org.postgresql.Driver";
    private static final String POSTGRESQL_USER_FIELD_NAME = "user";
    private static final String POSTGRESQL_PASSWORD_FIELD_NAME = "password";
    private Connection connection;
    private PreparedStatement validationQueryStatement;

    /**
     * Constructor by sql connection and options
     * @param options is options for connection
     * @throws StorageException Throw when object can't get connection for JDBC, or validation query down
     */
    public PostgresConnection(final ConnectionOptions options)
            throws StorageException {
        try {
            Class c = ModuleManager.getCurrentClassLoader().loadClass(POSTGRESQL_JDBC_DRIVER_NAME);
            Driver d = (Driver) c.newInstance();
            Properties prop = new Properties();
            prop.put(POSTGRESQL_USER_FIELD_NAME, options.getUsername());
            prop.put(POSTGRESQL_PASSWORD_FIELD_NAME, options.getPassword());
            Connection connection = d.connect(options.getUrl(), prop);
            connection.setAutoCommit(false);
            this.connection = connection;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new StorageException("Could not load JDBC driver.", e);
        } catch (SQLException | ReadValueException e) {
            throw new StorageException("Could not get JDBC connection.", e);
        }
        try {
            this.validationQueryStatement = connection.prepareStatement("SELECT(1);");
        } catch (SQLException e) {
            throw new StorageException("Error creating validation query for PostgreSQL database connection: ", e);
        }
    }

    /**
     *
     * @param preparedQuery the instance of IPreparedQuery
     * @return CompiledQuery
     * @throws StorageException Throw whe query can't be compiled for one of reason
     */
    public ICompiledQuery compileQuery(final IPreparedQuery preparedQuery)
            throws StorageException {
        try {
            return new JDBCCompiledQuery(((QueryStatement) preparedQuery).compile(connection));
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Error compiling query statement \"%s\": ",
                            ((QueryStatement) preparedQuery).getBodyWriter().toString()), e);
        }
    }

    /**
     * Check if the connection is valid.
     *
     * @return {@code true} if the connection is valid
     * @throws StorageException Throw whe query can't be compiled
     */
    public boolean validate()
            throws StorageException {
        try {
            if (connection.isClosed()) {
                return false;
            }

            try {
                validationQueryStatement.executeQuery();
            } catch (SQLException e) {
                try {
                    // After invalid SELECT queries Postgresql needs a rollback of transaction.
                    // So check if it's that case.
                    rollback();
                    validationQueryStatement.executeQuery();
                } catch (SQLException ee) {
                    e.addSuppressed(ee);
                    throw e;
                }
            }

            return true;
        } catch (SQLException e) {
            throw new StorageException("Error validating connection: ", e);
        }
    }

    /**
     * Close the connection
     *
     * @throws StorageException Throw when connection can't be closed
     */
    public void close()
            throws StorageException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new StorageException("Error closing JDBC connection: ", e);
        }
    }

    /**
     * Commit the current transaction.
     *
     * @throws StorageException Throw when connection have internal errors
     */
    public void commit()
            throws StorageException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new StorageException("Error committing SQL transaction: ", e);
        }
    }

    /**
     * Rollback the current  transaction.
     *
     * @throws StorageException Throw when rollback is damaged or changes in database can't be applied with rollback operation
     */
    public void rollback()
            throws StorageException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new StorageException("Error rolling back SQL transaction: ", e);
        }
    }
}
