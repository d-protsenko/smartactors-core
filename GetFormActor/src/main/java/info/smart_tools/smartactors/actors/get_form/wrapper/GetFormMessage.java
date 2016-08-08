package info.smart_tools.smartactors.actors.get_form.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for GetFormActor
 */
public interface GetFormMessage {
    /**
     * @return collection name
     * @exception ReadValueException Calling when try read value of variable
     */
    String getFormKey() throws ReadValueException;

    /**
     * Set form to message
     * @param form the form from cached collection
     * @exception ChangeValueException Calling when try change value of variable
     */
    void setForm(IObject form) throws ChangeValueException;
}
