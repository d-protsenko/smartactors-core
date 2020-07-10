package info.smart_tools.smartactors.database_postgresql.postgres_connection;

import info.smart_tools.smartactors.database.interfaces.istorage_connection.ICompiledQuery;

import java.sql.PreparedStatement;

/**
 * Implementation of {@link ICompiledQuery} wrapping the {@link PreparedStatement}.
 */
public class JDBCCompiledQuery implements ICompiledQuery {
    private final PreparedStatement preparedStatement;

    /**
     * default constructor
     * @param preparedStatement the statement for current connection
     */
    public JDBCCompiledQuery(final PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
