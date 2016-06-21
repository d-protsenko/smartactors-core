package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.time.LocalDateTime;

public interface UpsertIntoCachedCollectionQuery {

    LocalDateTime getStartDateTime() throws ReadValueException, ChangeValueException;
    void setStartDateTime(LocalDateTime startDateTime) throws ReadValueException, ChangeValueException;

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
