package info.smart_tools.smartactors.core.cached_collection.wrapper.upsert;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.time.LocalDateTime;

public interface UpsertItem {

    String getId() throws ReadValueException;
    Boolean isActive() throws ReadValueException;
    void setIsActive(Boolean isActive) throws ChangeValueException;
    String getKey() throws ReadValueException;
    LocalDateTime getStartDateTime() throws ReadValueException;
    void setStartDateTime(LocalDateTime startDateTime) throws ChangeValueException;

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
