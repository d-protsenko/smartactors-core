package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.IQueryStatementFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

abstract class CachedDatabaseTask extends GeneralDatabaseTask {

    private static final ConcurrentMap<Integer, ICompiledQuery> CACHED_COMPILED_QUERIES;

    private static final ConcurrentMap<String, IField> CACHED_DOCUMENTS_IDS;

    private static final int CACHE_MAX_SIZE;

    static {
        try {
            CACHED_COMPILED_QUERIES = new ConcurrentHashMap<>();
            CACHED_DOCUMENTS_IDS  = new ConcurrentHashMap<>();
            CACHE_MAX_SIZE = IOC.resolve(Keys.getOrAdd("CACHE_MAX_SIZE"));
        } catch (ResolutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected CachedDatabaseTask() {
        super();
    }

    protected ICompiledQuery takeCompiledQuery(final IKey queryKey,
                                               final IStorageConnection connection,
                                               final IQueryStatementFactory factory
    ) throws QueryBuildException {
        int hash = queryKey.hashCode();
        ICompiledQuery compiledQuery = CACHED_COMPILED_QUERIES.get(hash);
        if (compiledQuery != null) {
            return compiledQuery;
        }
        compiledQuery = createCompiledQuery(connection, factory);
        if (CACHED_COMPILED_QUERIES.size() >= CACHE_MAX_SIZE) {
            CACHED_COMPILED_QUERIES.clear();
        }
        CACHED_COMPILED_QUERIES.put(hash, compiledQuery);

        return compiledQuery;
    }

    protected IField getIdFieldFor(final String collection) throws ResolutionException {
        IField id = CACHED_DOCUMENTS_IDS.get(collection);
        if (id == null) {
            id = IOC.resolve(Keys.getOrAdd(IField.class.toString()), collection + "Id");
            CACHED_DOCUMENTS_IDS.put(collection, id);
        }

        return id;
    }

    public void clearCache() {
        CACHED_COMPILED_QUERIES.clear();
        CACHED_DOCUMENTS_IDS.clear();
    }
}
