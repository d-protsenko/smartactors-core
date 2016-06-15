package info.smart_tools.smartactors.core.postgres_connection;

import info.smart_tools.smartactors.core.istorage_connection.ICompiledQuery;
import java.sql.PreparedStatement;

/**
 * Implementation of {@link ICompiledQuery} wrapping the {@link PreparedStatement}.
 */
public class JDBCCompiledQuery implements ICompiledQuery {
    private final PreparedStatement preparedStatement;

    public JDBCCompiledQuery(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
