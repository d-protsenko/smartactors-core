package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.db_tasks.commons.queries.ComplexCompiledQuery;
import info.smart_tools.smartactors.core.db_tasks.commons.queries.IComplexCompiledQuery;
import info.smart_tools.smartactors.core.db_tasks.commons.queries.IComplexQueryStatementBuilder;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.DeclaredParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public abstract class ComplexDatabaseTask extends GeneralDatabaseTask {
    /**  */
    private final Map<Boolean, ParameterHandler> handlers;

    private static final ConcurrentMap<Integer, IComplexCompiledQuery> CACHED_COMPILED_QUERIES;

    private static final int CACHED_MAX_SIZE;

    static {
        try {
            CACHED_COMPILED_QUERIES = new ConcurrentHashMap<>();
            CACHED_MAX_SIZE = IOC.resolve(Keys.getOrAdd("COMPLEX_QUERY_CACHE_SIZE"));
        } catch (ResolutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void clearCache() {
        CACHED_COMPILED_QUERIES.clear();
    }

    /**
     *
     */
    protected ComplexDatabaseTask() {
        super();
        handlers = new HashMap<>(2);
        handlers.put(true, (parameter, sortedParams, declaredParam) -> {
            List<?> inParameters = (List<?>) parameter;
            if (inParameters.size() > declaredParam.getCount()) {
                throw new QueryBuildException("Invalid parameters: too many arguments for \"$in\"!");
            }
            sortedParams.addAll(inParameters);
            int variableSize = declaredParam.getCount() - inParameters.size();
            if (variableSize > 0) {
                for (int i = 0; i < variableSize; ++i) {
                    sortedParams.add(inParameters.get(inParameters.size() - 1));
                }
            }
        });

        handlers.put(false, (parameter, sortedParams, declaredParam) -> sortedParams.add(parameter));
    }

    protected ICompiledQuery takeCompiledQuery(final IKey queryKey,
                                               final IStorageConnection connection,
                                               final IComplexQueryStatementBuilder queryStatementBuilder
    ) throws QueryBuildException {
        List<DeclaredParam> declaredParams = new ArrayList<>();
        queryStatementBuilder.withDeclaredParams(declaredParams);

        int hash = queryKey.hashCode();
        IComplexCompiledQuery compiledQuery = CACHED_COMPILED_QUERIES.get(hash);
        if (compiledQuery != null) {
            return compiledQuery;
        }
        if (CACHED_COMPILED_QUERIES.size() > CACHED_MAX_SIZE) {
            CACHED_COMPILED_QUERIES.clear();
        }
        compiledQuery = ComplexCompiledQuery.create(
                createCompiledQuery(connection, queryStatementBuilder),
                declaredParams);
        CACHED_COMPILED_QUERIES.put(hash, compiledQuery);

        return compiledQuery;
    }

    /**
     *
     * @param query
     * @param message
     * @return
     * @throws QueryBuildException
     */
    protected List<Object> sortParameters(final ICompiledQuery query, final IObject message)
            throws QueryBuildException {
        try {
            IComplexCompiledQuery searchCompiledQuery = (IComplexCompiledQuery) query;
            IObject parameters = DBQueryFields.PARAMETERS.in(message);
            List<DeclaredParam> declaredParams = searchCompiledQuery.getDeclaredParams();

            List<Object> sortedParams = new ArrayList<>();
            for (DeclaredParam declaredParam : declaredParams) {
                Object parameter = parameters.getValue(declaredParam.getName());
                handlers.get(List.class.isAssignableFrom(parameter.getClass()))
                        .handle(parameter, sortedParams, declaredParam);
            }

            return  sortedParams;
        } catch (ClassCastException e) {
            throw new QueryBuildException("Expected complex query!");
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new QueryBuildException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    @FunctionalInterface
    private interface ParameterHandler {
        void handle(final Object parameter, final List<Object> sortedParams, final DeclaredParam declaredParam)
                throws QueryBuildException;
    }
}
