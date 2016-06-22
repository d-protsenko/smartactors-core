package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.cached_collection.GetItemStrategy;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface CachedCollectionConfig {

    IDatabaseTask getReadTask() throws ReadValueException, ChangeValueException;
    IDatabaseTask getUpsertTask() throws ReadValueException, ChangeValueException;
    IDatabaseTask getDeleteTask() throws ReadValueException, ChangeValueException;
    GetItemStrategy getStrategy() throws ReadValueException, ChangeValueException;
}
