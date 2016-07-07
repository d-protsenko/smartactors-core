package info.smart_tools.smartactors.core.pattern_matching;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.exception.PatternMatchingException;
import info.smart_tools.smartactors.core.imatcher.IMatcher;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;

import java.util.Iterator;
import java.util.Map;

/**
 * Checks availability of fields of pattern object in test object.
 * Non strict matching - test object must contain all fields from pattern object, test object can contain additional fields.
 */
public class PatternMatching implements IObject, IMatcher {

    private DSObject pattern;

    /**
     * Create new instance of {@link PatternMatching} by created pattern with pairs {@link IFieldName}, {@link Object}
     * @param objectEntries map of pairs {@link IFieldName}, {@link Object}
     * @throws InvalidArgumentException if argument is null
     */
    public PatternMatching(final Map<IFieldName, Object> objectEntries)
            throws InvalidArgumentException {
        pattern = new DSObject(objectEntries);
    }

    /**
     * Create new instance of {@link PatternMatching} by created pattern with serialized incoming string
     * @param body incoming string data
     * @throws InvalidArgumentException if any errors occurred on object creation
     */
    public PatternMatching(final String body)
            throws InvalidArgumentException {
        pattern = new DSObject(body);
    }

    /**
     * Create empty instance of {@link PatternMatching} with empty pattern
     */
    public PatternMatching() {
        pattern = new DSObject();
    }

    @Override
    public Object getValue(IFieldName name) throws ReadValueException, InvalidArgumentException {
        return pattern.getValue(name);
    }

    @Override
    public void setValue(IFieldName name, Object value) throws ChangeValueException, InvalidArgumentException {
        pattern.setValue(name, value);
    }

    @Override
    public void deleteField(IFieldName name) throws DeleteValueException, InvalidArgumentException {
        pattern.deleteField(name);
    }

    @Override
    public <T> T serialize() throws SerializeException {
        return pattern.serialize();
    }

    @Override
    public Iterator<Map.Entry<IFieldName, Object>> iterator() {
        return pattern.iterator();
    }

    @Override
    public Boolean match(IObject obj) throws ReadValueException, InvalidArgumentException, PatternMatchingException {
        if (obj == null)
            throw new PatternMatchingException("Test object must be specified.");
        Iterator<Map.Entry<IFieldName, Object>> iterator = iterator();
        if (!iterator.hasNext())
            throw new PatternMatchingException("Pattern can not be empty. Pattern must contain several fields to compare.");
        Map.Entry<IFieldName, Object> patternField;
        IFieldName patternFieldName;
        Object patternFieldValue;
        while (iterator.hasNext()) {
            patternField = iterator.next();
            patternFieldName = patternField.getKey();
            patternFieldValue = patternField.getValue();
            if (!identical(patternFieldValue, obj.getValue(patternFieldName)))
                return false;
        }
        return true;
    }

    private Boolean identical(Object patternFieldValue, Object testObjectFieldValue) throws PatternMatchingException {
        if (patternFieldValue == null)
            throw new PatternMatchingException("Pattern field value can not be null.");
        return patternFieldValue.equals(testObjectFieldValue);
    }
}
