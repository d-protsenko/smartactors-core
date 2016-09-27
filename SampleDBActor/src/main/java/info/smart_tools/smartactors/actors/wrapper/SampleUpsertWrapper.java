package info.smart_tools.smartactors.actors.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface SampleUpsertWrapper {

    String getCollectionName() throws ReadValueException;

    IObject getDocument() throws ReadValueException;

    void setDocument(IObject result) throws ChangeValueException;

}
