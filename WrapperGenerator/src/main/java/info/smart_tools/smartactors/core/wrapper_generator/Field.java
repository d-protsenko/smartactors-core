package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * Class that contains rule for convert
 * @param <T> the type to cast FieldName value to
 */
public class Field<T> {
    private IFieldName name;

    /**
     * Construct instance of {@link Field} by instance of {@link IFieldName}
     * @param name instance of {@link IFieldName}
     */
    public Field(final IFieldName name) {
        this.name = name;
    }

    /**
     * Get value from instance of {@link IObject} by {@link IFieldName}
     * and cast it to {@code targetClass}
     * @param object instance of {@link IObject}
     * @param targetClass class to cast to
     * @return converted value
     * @throws ReadValueException if any errors occurred on reading value from object
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if object are null
     */
    public T from(final IObject object, final Class<T> targetClass)
            throws ReadValueException, ChangeValueException, InvalidArgumentException {
        if (object == null) {
            throw new InvalidArgumentException("Input IObject in Field.from(IObject, Class) is null");
        }

        return convert(object, targetClass);
    }

    private T convert(final IObject object, final Class<T> targetClass)
            throws ReadValueException, ChangeValueException, InvalidArgumentException {
        Object value = object.getValue(name);
        if (value == null) {
            return null;
        }

        if (value.getClass().equals(targetClass)) {
            return (T) value;
        }

        try {
            T converted = IOC.resolve(Keys.getOrAdd(value.getClass().toString()), targetClass, value);
            object.setValue(name, converted);
            return converted;
        } catch (ResolutionException e) {
            throw new InvalidArgumentException("Passed object with value of field which is impossible to resolve to expected type", e);
        }
    }

    /**
     * Insert into {@link IObject} value-object that casts to target class.
     * @param object IObject that wil be contain value
     * @param value value object
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if incoming arguments are null or invalid
     */
    public void inject(final IObject object, final T value)
            throws ChangeValueException, InvalidArgumentException {
        object.setValue(name, value);
    }

    /**
     * Insert into {@link IObject} value-object that casts to target class.
     * @param object IObject that wil be contain value
     * @param value value object
     * @param targetClass final conversion class type
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if incoming arguments are null or invalid
     * @throws ResolutionException if resolution was failed
     */
    public void inject(final IObject object, final Object value, final Class<? extends T> targetClass)
            throws ChangeValueException, InvalidArgumentException, ResolutionException {
        T converted = IOC.resolve(Keys.getOrAdd(value.getClass().toString()), targetClass.toString(), value);
        object.setValue(name, converted);
    }

    /**
     * Delete from input {@link IObject} field {@link Field#name}
     * @param object input object
     * @throws DeleteValueException if any errors occurred on deleting value from object
     * @throws InvalidArgumentException if incoming arguments are null or invalid
     */
    public void delete(final IObject object)
            throws DeleteValueException, InvalidArgumentException {
        object.deleteField(name);
    }

}
