package info.smart_tools.smartactors.core.cached_collection.wrapper.upsert;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.time.LocalDateTime;

public interface UpsertItem {

    String getId() throws ReadValueException, ChangeValueException;
    Boolean isActive() throws ReadValueException, ChangeValueException;
    void setIsActive(Boolean isActive) throws ReadValueException, ChangeValueException;
    String getKey() throws ReadValueException, ChangeValueException;
    LocalDateTime getStartDateTime() throws ReadValueException, ChangeValueException;
    void setStartDateTime(LocalDateTime startDateTime) throws ReadValueException, ChangeValueException;

    IObject wrapped() throws ReadValueException, ChangeValueException;
}
