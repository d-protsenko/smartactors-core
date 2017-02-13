package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.SQLQueryParameterSetter;
import rx.Observable;

import java.io.StringWriter;
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
     */
    public Observable<ResultSet> execute(final Db db) {
        StringBuffer buf = bodyWriter.getBuffer();

        AsyncQueryUtils.reformatBuffer(buf);

        return db.querySet(buf.toString(), queryParameterSetters.toArray());
    }
}
