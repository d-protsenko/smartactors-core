package info.smart_tools.smartactors.core.postgres_connection;

import info.smart_tools.smartactors.core.istorage_connection.ICompiledQuery;
import info.smart_tools.smartactors.core.istorage_connection.IPreparedQuery;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Implementation of {@link IStorageConnection}
 */
public class PostgresConnection implements IStorageConnection {
    private Connection connection;
    private ConnectionOptions options;
    private PreparedStatement validationQueryStatement;

    /**
     * Constructor by sql connection and options
     * @param connection the JDBC connection
     * @param options is options for connection
     * @throws StorageException
     */
     public PostgresConnection(final Connection connection, final ConnectionOptions options)
             throws StorageException {
         this.connection = connection;
         this.options = options;

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
