package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface CachedCollectionParameters {

    IDatabaseTask getTask() throws ReadValueException, ChangeValueException;
    IObject getQuery() throws ReadValueException, ChangeValueException;
}
