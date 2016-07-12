package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.queries.IQueryStatementBuilder;
import info.smart_tools.smartactors.core.ikey.IKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class CachedDatabaseTask extends GeneralDatabaseTask {

    private static final ConcurrentMap<Integer, ICompiledQuery> CACHED_COMPILED_QUERIES = new ConcurrentHashMap<>();

    protected CachedDatabaseTask() {
        super();
    }

    protected ICompiledQuery takeCompiledQuery(final IKey queryKey,
                                               final IStorageConnection connection,
                                               final IQueryStatementBuilder queryStatementBuilder
    ) throws QueryBuildException {
        int hash = queryKey.hashCode();
        ICompiledQuery compiledQuery = CACHED_COMPILED_QUERIES.get(hash);
        if (compiledQuery != null) {
            return compiledQuery;
        }
        compiledQuery = createCompiledQuery(connection, queryStatementBuilder);
        CACHED_COMPILED_QUERIES.put(hash, compiledQuery);

        return compiledQuery;
    }


    public void clearCache() {
        CACHED_COMPILED_QUERIES.clear();
    }
}
