package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.SQLQueryParameterSetter;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.SQLQueryParameters;
import rx.Observable;

import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AsyncQueryStatement extends QueryStatement {
    private final StringWriter bodyWriter = new StringWriter();
    private final List<SQLQueryParameterSetter> queryParameterSetters = new ArrayList<>();

    @Override
    public StringWriter getBodyWriter() {
        return bodyWriter;
    }

    @Override
    public void pushParameterSetter(final SQLQueryParameterSetter setter) {
        queryParameterSetters.add(setter);
    }

    /**
     * Execute the query on given database.
     *
     * @param db the database
     * @return @see {@link Db#querySet(String, Object...)}
     * @throws Exception if any error occurs
     */
    public Observable<ResultSet> execute(final Db db)
            throws Exception {
        StringBuffer buf = bodyWriter.getBuffer();

        int nParams = AsyncQueryUtils.reformatBuffer(buf);

        Object[] params = new Object[nParams];

        SQLQueryParameters parameters = new SQLQueryParameters() {
            @Override
            public void setString(final int index, final String val) throws SQLException {
                params[index] = val;
            }

            @Override
            public void setObject(final int index, final Object val) throws SQLException {
                params[index] = val;
            }

            @Override
            public void setInt(final int index, final int val) throws SQLException {
                params[index] = val;
            }
        };

        int idx = 0;

        for (SQLQueryParameterSetter setter : queryParameterSetters) {
            idx = setter.setParameters(parameters, idx);
        }

        return db.querySet(buf.toString(), params);
    }
}
