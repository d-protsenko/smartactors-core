package info.smart_tools.smartactors.actors.validate_form_data;

import info.smart_tools.smartactors.actors.validate_form_data.exception.ValidateFormException;
import info.smart_tools.smartactors.actors.validate_form_data.wrapper.ValidateFormDataMessage;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import javax.xml.validation.ValidatorHandler;
import java.util.Iterator;
import java.util.Map;

/**
 * Actor for validating form data from client
 */
public class ValidateFormDataActor {
    /**
     * Constructor
     * @param params
     */
    public ValidateFormDataActor(final IObject params) {}

    /**
     * Validate form data from client
     * @param message the message
     */
    public void validate(final ValidateFormDataMessage message) throws ValidateFormException {
        try {
            IObject formFields = message.getForm();
            IObject clientData = message.getFormFromRequest();

            IObject resultObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

            Iterator<Map.Entry<IKey, Object>> fieldsIterator = formFields.iterator();
            Map.Entry<IKey, Object> entry = fieldsIterator.next();

            while (entry != null) {
                IKey key = entry.getKey();
                resultObject.setValue(key, clientData.getValue(key));

                if (!fieldsIterator.hasNext()) {
                    break;
                }
                entry = fieldsIterator.next();
            }
            message.setFormData(resultObject);
        } catch (Exception e) {
            throw new ValidateFormException(e);
        }
    }
}
