package info.smart_tools.smartactors.database_postgresql.postgres_connection;

import info.smart_tools.smartactors.database.interfaces.istorage_connection.IPreparedQuery;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 *  Stores a text of SQL statement and list of {@link SQLQueryParameterSetter}'s which should be used on
 *  {@link PreparedStatement} created using this text.
 */
public class QueryStatement implements IPreparedQuery {
    private StringWriter bodyWriter;
    private List<SQLQueryParameterSetter> parameterSetters;

    /**
     * Boring constructor
     */
    public QueryStatement() {
        this.bodyWriter = new StringWriter();
        this.parameterSetters = new LinkedList<>();
    }

    /**
     *  @return Writer where to write statement text.
     */
    public Writer getBodyWriter() {
        return bodyWriter;
    }

    /**
     *  Add {@link SQLQueryParameterSetter} to list of setters to be used.
     *  @param setter setter to add.
     */
    public void pushParameterSetter(final SQLQueryParameterSetter setter) {
        parameterSetters.add(setter);
    }

    /**
     *  Creates {@link PreparedStatement} ad applies all {@link SQLQueryParameterSetter}'s on it.
     *
     *  @param connection database connection to use for statement creation.
     *  @return created statement.
     *  @throws SQLException Throw when can't prepare statement
     */
    public PreparedStatement compile(final Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(this.bodyWriter.toString());

        int index = 1;

        SQLQueryParameters statementParameters = new SQLQueryParameters() {
            @Override
            public void setString(final int index, final String val)
                    throws SQLException {
                stmt.setString(index, val);
            }

            @Override
            public void setObject(final int index, final Object val)
                    throws SQLException {
                stmt.setObject(index, val);
            }

            @Override
            public void setInt(final int index, final int val)
                    throws SQLException {
                stmt.setInt(index, val);
            }
        };

        for (SQLQueryParameterSetter setter : this.parameterSetters) {
            index = setter.setParameters(statementParameters, index);
        }

        return stmt;
    }
}
