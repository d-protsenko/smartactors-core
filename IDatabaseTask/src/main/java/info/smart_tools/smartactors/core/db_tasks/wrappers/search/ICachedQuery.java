package info.smart_tools.smartactors.core.db_tasks.wrappers.search;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;

import java.util.List;

/**
 * A buffered search query.
 */
public interface ICachedQuery {
    /**
     * @return the compiled query with empty parameters.
     */
    CompiledQuery getCompiledQuery();

    /**
     *
     * @param query
     */
    void setCompiledQuery(CompiledQuery query);

    /**
     * @return the parameters setters for compiled query.
     */
    List<SQLQueryParameterSetter> getParametersSetters();

    /**
     *
     * @param setters
     */
    void setParametersSetters(List<SQLQueryParameterSetter> setters);
}
