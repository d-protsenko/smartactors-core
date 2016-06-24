package info.smart_tools.smartactors.core.cached_collection.wrapper.delete;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface DeleteItem {

    String getId() throws ReadValueException, ChangeValueException;
    String getKey() throws ReadValueException, ChangeValueException;
    void setIsActive(Boolean isActive) throws ReadValueException, ChangeValueException;
}
