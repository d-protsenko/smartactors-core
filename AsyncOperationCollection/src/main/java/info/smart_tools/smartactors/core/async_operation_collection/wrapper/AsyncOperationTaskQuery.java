package info.smart_tools.smartactors.core.async_operation_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface AsyncOperationTaskQuery {
    void setToken(EQMessage query) throws ReadValueException, ChangeValueException;// TODO: write custom name for field
}
