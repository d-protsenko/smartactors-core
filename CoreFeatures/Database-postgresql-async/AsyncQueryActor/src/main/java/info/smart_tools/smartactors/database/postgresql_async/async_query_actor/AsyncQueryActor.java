package info.smart_tools.smartactors.database.postgresql_async.async_query_actor;

import com.github.pgasync.ConnectionPoolBuilder;
import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import com.sun.org.apache.xerces.internal.util.URI;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl.AsyncQueryStatement;
import info.smart_tools.smartactors.database.postgresql_async.async_query_actor.wrappers.InsertMessage;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.PostgresSchema;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import rx.Observable;

/**
 *
 */
public class AsyncQueryActor {
    private final Db db;

    /**
     *
     * @param uri         database URI: pgsql:///user:password@host/database
     * @param poolSize    size of connection pool
     * @throws Exception if any error occurs. Really. I cannot know what may throw the 3-rd party library this actor uses.
     */
    // TODO: Use ConnectionOptions
    public AsyncQueryActor(final URI uri, final int poolSize)
            throws Exception {
        db = new ConnectionPoolBuilder()
                .hostname(uri.getHost())
                .port(uri.getPort())
                .database(uri.getPath())
                .username(uri.getUserinfo().split(":")[0])
                .password(uri.getUserinfo().split(":")[1])
                .poolSize(poolSize)
                .build();
    }

    /**
     *
     * @param message    the message
     * @throws Exception if any error occurs
     */
    public void insert(final InsertMessage message)
            throws Exception {
        String serializedDoc = message.getDocument().serialize();
        AsyncQueryStatement statement = new AsyncQueryStatement();

        PostgresSchema.insert(statement, CollectionName.fromString(message.getCollectionName()));

        statement.pushParameterSetter(((parameters, firstIndex) -> {
            parameters.setString(firstIndex, serializedDoc);
            return firstIndex + 1;
        }));

        Observable<ResultSet> resObservable = statement.execute(db);

        IMessageProcessor mp = message.getProcessor();

        mp.pauseProcess();

        try {
            resObservable
                    .subscribe(
                            set -> {
                                try {
                                    mp.continueProcess(null);
                                } catch (AsynchronousOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            err -> {
                                try {
                                    mp.continueProcess(err);
                                } catch (AsynchronousOperationException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
        } catch (Exception e) {
            try {
                mp.continueProcess(e);
            } catch (AsynchronousOperationException ee) {
                e.addSuppressed(ee);
            }
            throw e;
        }
    }
}
