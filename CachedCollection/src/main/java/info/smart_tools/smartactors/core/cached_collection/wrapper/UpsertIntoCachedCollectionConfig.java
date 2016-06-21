package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface UpsertIntoCachedCollectionConfig {

    IDatabaseTask getUpsertTask() throws ReadValueException, ChangeValueException;
    String getKey() throws ReadValueException, ChangeValueException;
}
