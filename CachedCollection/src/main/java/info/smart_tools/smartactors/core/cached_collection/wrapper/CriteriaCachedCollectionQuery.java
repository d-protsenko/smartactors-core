package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.cached_collection.wrapper.get_item.DateToMessage;
import info.smart_tools.smartactors.core.cached_collection.wrapper.get_item.EQMessage;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

public interface CriteriaCachedCollectionQuery {
    void setStartDateTime(DateToMessage query) throws ChangeValueException;
    void setIsActive(EQMessage query) throws ChangeValueException;
    void setKey(EQMessage query) throws ChangeValueException;// TODO: write custom name for field

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    void wrapped() throws ChangeValueException;
}
