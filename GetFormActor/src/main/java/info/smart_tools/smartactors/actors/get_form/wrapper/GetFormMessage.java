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
     */
    String getFormKey() throws ReadValueException;

    /**
     * Set form to message
     * @param form the form from cached collection
     */
    void setForm(IObject form) throws ChangeValueException;
}
