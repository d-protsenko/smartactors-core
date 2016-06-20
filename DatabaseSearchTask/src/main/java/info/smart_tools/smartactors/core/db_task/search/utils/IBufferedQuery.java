package info.smart_tools.smartactors.core.db_task.search.utils;

import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;

import java.util.List;

/**
 *
 */
public interface IBufferedQuery {
    /**  */
    CompiledQuery getCompiledQuery();
    void setCompiledQuery(CompiledQuery query);

    /**  */
    List<SQLQueryParameterSetter> getParametersSetters();
    void setParametersSetters(List<SQLQueryParameterSetter> setters);
}
