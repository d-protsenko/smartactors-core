package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface GetObjectsFromCachedCollectionParameters {
    IDatabaseTask getTask() throws ReadValueException, ChangeValueException;
    String getCollectionName() throws ReadValueException, ChangeValueException;
}
