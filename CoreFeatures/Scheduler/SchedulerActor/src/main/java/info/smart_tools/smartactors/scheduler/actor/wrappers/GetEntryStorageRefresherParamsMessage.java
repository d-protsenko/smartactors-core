package info.smart_tools.smartactors.scheduler.actor.wrappers;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;

public interface GetEntryStorageRefresherParamsMessage {
    void setRefresherParams(IObject params) throws ChangeValueException;
}
