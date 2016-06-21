package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;

public interface GetObjectsFromCachedCollectionParameters {
    IDatabaseTask getTask();
    String getKey();
    String getCollectionName();
}
