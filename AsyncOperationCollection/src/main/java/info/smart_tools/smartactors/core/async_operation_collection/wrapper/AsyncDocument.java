package info.smart_tools.smartactors.core.async_operation_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface AsyncDocument {
    void setToken(String token) throws ChangeValueException;
    void setAsyncData(IObject data) throws ChangeValueException;
    void setExpiredTime(String time) throws ChangeValueException;
    void setDoneFlag(Boolean flag) throws ChangeValueException;
    IObject getIObject() throws ReadValueException;
}
