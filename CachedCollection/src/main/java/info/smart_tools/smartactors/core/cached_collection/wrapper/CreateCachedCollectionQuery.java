package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Map;

public interface CreateCachedCollectionQuery {

    String getKey() throws ReadValueException, ChangeValueException;

    void setIndexes(Map<String, String> indexes) throws ReadValueException, ChangeValueException;

    //NOTE:: this is proto of method instead of extractWrapped() from IObjectWrapper
    IObject wrapped() throws ReadValueException, ChangeValueException;
}
