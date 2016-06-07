package info.smart_tools.smartactors.core.db_task.get_by_id.psql.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for SearchById message
 */
public interface SearchByIdQuery {
    String getCollectionName() throws ReadValueException, ChangeValueException;
}
