package info.smart_tools.smartactors.core.cached_collection.wrapper.DBSearchWrappers;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface EQMessage {
    void setEq(String val) throws ReadValueException, ChangeValueException;// TODO: write custom name for field
}
