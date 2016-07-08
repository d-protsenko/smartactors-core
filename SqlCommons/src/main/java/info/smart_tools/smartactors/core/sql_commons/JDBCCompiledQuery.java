package info.smart_tools.smartactors.core.sql_commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link ICompiledQuery} wrapping the {@link PreparedStatement}.
 */
public class JDBCCompiledQuery implements ICompiledQuery {
    private final PreparedStatement preparedStatement;

    public JDBCCompiledQuery(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    @Override
    public boolean execute() throws QueryExecutionException {
        try {
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public ResultSet executeQuery() throws QueryExecutionException {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public int executeUpdate() throws QueryExecutionException {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new QueryExecutionException(e.getMessage(), e);
        }
    }

    @Override
    public void setParameters(List<ISQLQueryParameterSetter> parameterSetters) throws QueryBuildException {
        int index = 1;
        try {
            for (ISQLQueryParameterSetter setter : parameterSetters) {
                index = setter.setParameters(this.preparedStatement, index);
            }
        } catch (SQLException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }
}
