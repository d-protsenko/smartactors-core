package info.smart_tools.smartactors.actors.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface SampleWrapper {

    String getSomeField() throws ReadValueException;

    void setSomeValueForRequest(String value) throws ChangeValueException;

    void setCurrentActorState(Integer state) throws ChangeValueException;

    Boolean resetState() throws ReadValueException;
}
