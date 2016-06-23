package info.smart_tools.smartactors.core.cached_collection.wrapper.DBSearchWrappers;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface DateToMessage {
    void setDateTo(String dateTo) throws ReadValueException, ChangeValueException;// TODO: write custom name for field
}
