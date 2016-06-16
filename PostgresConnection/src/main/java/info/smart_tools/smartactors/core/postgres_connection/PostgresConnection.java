package info.smart_tools.smartactors.core.postgres_connection;

import info.smart_tools.smartactors.core.istorage_connection.ICompiledQuery;
import info.smart_tools.smartactors.core.istorage_connection.IPreparedQuery;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Implementation of {@link IStorageConnection}
 */
public class PostgresConnection implements IStorageConnection {
    private static final  String POSTGRESQL_JDBC_DRIVER_NAME = "org.postgresql.Driver";
    private Connection connection;
    private PreparedStatement validationQueryStatement;
    private static final Properties initProps = new Properties();

    static {
        try (InputStream src = PostgresConnection.class.getResourceAsStream("db-init.properties")) {
            initProps.load(src);
        } catch (IOException e) {
        }
    }

    /**
     * Constructor by sql connection and options
     * @param options is options for connection
     * @throws StorageException
     */
    public PostgresConnection(final ConnectionOptions options) throws StorageException {
        try {
            Connection connection;

            try {
                Class.forName(POSTGRESQL_JDBC_DRIVER_NAME);
            } catch (ClassNotFoundException e) {
                throw new StorageException("Could not load JDBC driver.", e);
            }

            try {
                connection = DriverManager.getConnection(
                        options.getUrl(),
                        options.getUsername(),
                        options.getPassword());
            } catch (SQLException e) {
                throw new StorageException("Could not get JDBC connection.", e);
            }

            try {
                for (Object key : initProps.keySet()) {
                    connection.createStatement().execute(initProps.getProperty((String) key));
                }

                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new StorageException("Error configuring JDBC connection: ", e);
            }

            this.connection = connection;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }

        try {
            this.validationQueryStatement = connection.prepareStatement("SELECT(1);");
        } catch (SQLException e) {
            throw new StorageException("Error creating validation query for PostgreSQL database connection: ", e);
        }
    }

    /**
     *
     * @param preparedQuery
     * @return CompiledQuery
     * @throws StorageException
     */
    public ICompiledQuery compileQuery(IPreparedQuery preparedQuery) throws StorageException {
        try {
            return new JDBCCompiledQuery(((QueryStatement) preparedQuery).compile(connection));
        } catch (SQLException e) {
            throw new StorageException(
                    String.format("Error compiling query statement \"%s\": ",
                            ((QueryStatement)preparedQuery).bodyWriter.toString()), e);
        }
    }

    public boolean validate() throws StorageException {
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

    public void close()
            throws StorageException {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new StorageException("Error closing JDBC connection: ", e);
        }
    }

    public void commit()
            throws StorageException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new StorageException("Error committing SQL transaction: ", e);
        }
    }

    public void rollback()
            throws StorageException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new StorageException("Error rolling back SQL transaction: ", e);
        }
    }
}
