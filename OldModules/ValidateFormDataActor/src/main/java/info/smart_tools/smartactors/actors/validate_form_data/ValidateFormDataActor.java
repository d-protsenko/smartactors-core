package info.smart_tools.smartactors.actors.validate_form_data;

import info.smart_tools.smartactors.actors.validate_form_data.exception.ValidateFormException;
import info.smart_tools.smartactors.actors.validate_form_data.parser.Parser;
import info.smart_tools.smartactors.actors.validate_form_data.wrapper.ValidateFormDataMessage;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Actor for validating form data from client
 */
public class ValidateFormDataActor {
    private Field validationRulesF;
    private Field ruleF;

    /**
     * Constructor
     * @param params the empty IObject
     * @exception InvalidArgumentException Calling when throw any exception
     */
    public ValidateFormDataActor(final IObject params) throws InvalidArgumentException {
        try {
            validationRulesF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "validationRules");
            ruleF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "rule");

        } catch (Exception e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Validate form data from client
     * @param message the message
     * @throws ValidateFormException if form is not valid
     */
    public void validate(final ValidateFormDataMessage message) throws ValidateFormException {
        try {
            IObject formFields = message.getForm();
            IObject clientData = message.getFormFromRequest();

            IObject resultObject = IOC.resolve(Keys.getOrAdd("info.smart_tools.smartactors.iobject.iobject.IObject"));

            Iterator<Map.Entry<IFieldName, Object>> fieldsIterator = formFields.iterator();
            Map.Entry<IFieldName, Object> entry = fieldsIterator.next();

            while (entry != null) {
                IFieldName key = entry.getKey();

                List<IObject> rules = validationRulesF.in((IObject) formFields.getValue(key));
                if (rules != null) {
                    for (IObject ruleObject : rules) {
                        if (!new Parser((String) ruleF.in(ruleObject), (String) clientData.getValue(key)).validate()) {
                            throw new ValidateFormException("Fields is not correct");
                        }
                    }
                }
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
