package info.smart_tools.smartactors.core.db_tasks.commons.queries;

import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.DeclaredParam;

import java.util.List;

/**
 * 
 */
public interface IComplexCompiledQuery extends ICompiledQuery {
    /**
     * 
     * @return
     */
    List<DeclaredParam> getDeclaredParams();
}
