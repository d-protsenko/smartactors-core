package info.smart_tools.smartactors.core.postgres_connection;

import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.pool.Pool;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

public class PostgresConnectionPoolCreator {
    private final static String POSTGRESQL_JDBC_DRIVER_NAME = "org.postgresql.Driver";
    private ConnectionOptions connectionOptions;
    private static final Properties initProps = new Properties();

    static {
        try(InputStream src = PostgresConnectionPoolCreator.class.getResourceAsStream("db-init.properties")) {
            initProps.load(src);
        } catch (IOException e) {}
    }


    public PostgresConnectionPoolCreator(ConnectionOptions options) {
        this.connectionOptions = options;
    }

    public Pool createPool() {
        return new Pool(connectionOptions.getMaxConnections(), new Supplier<Object>() {
            @Override
            public IStorageConnection get() {
                try {
                    Connection connection;

                    try {
                        Class.forName(POSTGRESQL_JDBC_DRIVER_NAME);
                    } catch (ClassNotFoundException e) {
                        throw new StorageException("Could not load JDBC driver.", e);
                    }

                    try {
                        connection = DriverManager.getConnection(
                                connectionOptions.getUrl(),
                                connectionOptions.getUsername(),
                                connectionOptions.getPassword());
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

                    return new PostgresConnection(connection, connectionOptions);
                } catch (StorageException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
