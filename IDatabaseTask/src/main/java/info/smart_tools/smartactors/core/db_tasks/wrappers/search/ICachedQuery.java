package info.smart_tools.smartactors.core.db_tasks.wrappers.search;

import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.ISQLQueryParameterSetter;

import java.util.List;

/**
 * A buffered search query.
 */
public interface ICachedQuery {
    /**
     * @return the compiled query with empty parameters.
     */
    ICompiledQuery getCompiledQuery();

    /**
     *
     * @param query
     */
    void setCompiledQuery(ICompiledQuery query);

    /**
     * @return the parameters setters for compiled query.
     */
    List<ISQLQueryParameterSetter> getParametersSetters();

    /**
     *
     * @param setters
     */
    void setParametersSetters(List<ISQLQueryParameterSetter> setters);
}
