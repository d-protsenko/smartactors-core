package info.smart_tools.smartactors.core.async_operation_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

public interface EQMessage {
    void setEq(String val) throws ChangeValueException;
}
