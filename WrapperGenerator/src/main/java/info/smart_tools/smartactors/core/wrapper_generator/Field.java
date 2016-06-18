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
 * Support class for convert types and resolve dependencies in
 * @param <T> type of returning value for getters or type of value that will be set to iobject
 */
public class Field<T> {
    private IFieldName name;

    /**
     * Constructor.
     * Create instance of {@link Field} by given instance of {@link IFieldName}
     * @param name instance of {@link IFieldName}
     */
    public Field(final IFieldName name) {
        this.name = name;
    }

    /**
     * Get specific value from instance of {@link IObject} and convert it to T type
     * @param object instance of {@link IObject}
     * @param targetClass class to resolved value convert to
     * @return instance of {@link T}
     * @throws ReadValueException if any errors occurred when iobject had been reading
     * @throws ChangeValueException if any errors occurred when iobject had been changing
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    public T from(final IObject object, final Class<T> targetClass)
            throws ReadValueException, ChangeValueException, InvalidArgumentException {
        if (object == null) {
            throw new InvalidArgumentException("Input IObject in Field.from(IObject, Class) is null");
        }

        return resolve(object, targetClass);
    }

    private T resolve(final IObject object, final Class<T> targetClass)
            throws ReadValueException, ChangeValueException, InvalidArgumentException {
        Object value = object.getValue(name);
        if (value == null) {
            return null;
        }

        if (value.getClass().equals(targetClass)) {
            return (T) value;
        }

        try {
            T converted = IOC.resolve(Keys.getOrAdd(targetClass.toString()), value);
            object.setValue(name, converted);
            return converted;
        } catch (ResolutionException e) {
            throw new InvalidArgumentException(
                    "Passed object with value of field which is impossible to resolve to expected type",
                    e
            );
        }
    }

    /**
     * Insert into {@link IObject} value-object that casts to target class.
     * @param object IObject that wil be contain value
     * @param value value object
     * @throws ChangeValueException if any errors occurred when iobject had been changing
     * @throws InvalidArgumentException if incoming arguments are incorrect
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
     * @throws ChangeValueException if any errors occurred when iobject had been changing
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    public void inject(final IObject object, final Object value, final Class<? extends T> targetClass)
            throws ChangeValueException, InvalidArgumentException {
        try {
            T converted = IOC.resolve(Keys.getOrAdd(targetClass.toString()), value);
            object.setValue(name, converted);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException(
                    "Passed object with value of field which is impossible to resolve to expected type",
                    e
            );
        }
    }

    /**
     * Delete from input {@link IObject} field {@link Field#name}
     * @param object input object
     * @throws DeleteValueException if any errors occurred when iobject had been deleting
     * @throws InvalidArgumentException if incoming arguments are incorrect
     */
    public void delete(final IObject object)
            throws DeleteValueException, InvalidArgumentException {
        object.deleteField(name);
    }

}
