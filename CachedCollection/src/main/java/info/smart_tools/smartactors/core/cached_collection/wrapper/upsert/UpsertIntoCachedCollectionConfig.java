package info.smart_tools.smartactors.core.cached_collection.wrapper.upsert;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Config for upsert task-facade
 */
public interface UpsertIntoCachedCollectionConfig {

    IDatabaseTask getUpsertTask() throws ReadValueException, ChangeValueException;
    String getKey() throws ReadValueException, ChangeValueException;
}
