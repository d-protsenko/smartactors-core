package info.smart_tools.smartactors.core.cached_collection.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.Optional;

public interface CachedItem {

    String getId() throws ReadValueException, ChangeValueException;
    Optional<Boolean> isActive() throws ReadValueException, ChangeValueException;

    IObject wrapped() throws ReadValueException, ChangeValueException;


}
