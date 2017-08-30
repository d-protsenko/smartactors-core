package info.smart_tools.smartactors.actors.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface SampleGetByIdWrapper {

    String getCollectionName() throws ReadValueException;

    Object getDocumentId() throws ReadValueException;

    void setDocument(IObject document) throws ChangeValueException;

}
