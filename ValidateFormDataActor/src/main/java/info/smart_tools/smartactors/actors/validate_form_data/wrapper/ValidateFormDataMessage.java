package info.smart_tools.smartactors.actors.validate_form_data.wrapper;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Message wrapper
 */
public interface ValidateFormDataMessage {
    /**
     * getter
     * @return form data from client
     * @throws ReadValueException Calling when try read value of variable
     */
    IObject getFormFromRequest() throws ReadValueException;

    /**
     * Getter
     * @return form parameters from async operation
     * @throws ReadValueException Calling when read change value of variable
     */
    IObject getForm() throws ReadValueException;

    /**
     * Setter
     * @param form valid form
     * @throws ReadValueException Calling when try read value of variable
     * @throws ChangeValueException Calling when try change value of variable
     */
    void setFormData(IObject form) throws ReadValueException, ChangeValueException;
}
