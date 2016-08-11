package info.smart_tools.smartactors.actors.sample_other_actor.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface SampleOtherWrapper {

    String getStringValue() throws ReadValueException;
}
