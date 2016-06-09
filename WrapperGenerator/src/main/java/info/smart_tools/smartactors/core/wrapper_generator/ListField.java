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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for conversion predefined lists to list of specific type
 * @param <T> specific type to cast to list of T
 */
public class ListField<T> {

    private IFieldName name;

    /**
     * Construct {@code ListField} by given name
     * @param name given name
     */
    public ListField(final IFieldName name) {
        this.name = name;
    }

    /**
     * Convert predefined list in current field to list of specific type
     * @param object data object
     * @param targetClass class of target type
     * @return list of specific type
     * @throws ReadValueException if any errors occurred on reading value from object
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if arguments are null or invalid
     * @throws ResolutionException if resolution was failed
     */
    public List<T> from(final IObject object, final Class<T> targetClass)
            throws ReadValueException, ChangeValueException, InvalidArgumentException, ResolutionException {

        if (object == null || targetClass == null) {
            throw new InvalidArgumentException("Input parameters in Field.from(IObject, targetClass) method are null");
        }
        Object value = object.getValue(name);
        if (value == null) {
            return null;
        }
        List fieldList = (List) value;
        if (fieldList.isEmpty()) {
            return new LinkedList<>();
        }
        if (fieldList.get(0).getClass().equals(targetClass)) {
            return (List<T>) fieldList;
        }
        List<T> targetList = new ArrayList<>(fieldList.size());
        for (Object listItem : fieldList) {
            T converted = IOC.resolve(Keys.getOrAdd(targetClass.toString()), listItem);
            targetList.add(converted);
        }
        object.setValue(name, targetList);

        return targetList;
    }

    /**
     * Insert into {@link IObject} value-object that casts to target class.
     * @param object IObject that wil be contain value
     * @param list list object
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if arguments are null or invalid
     */
    public void inject(final IObject object, final List<T> list)
            throws ChangeValueException, InvalidArgumentException {
        object.setValue(name, list);
    }

    /**
     * Insert into {@link IObject} value-object that casts to target class.
     * @param object IObject that wil be contain value
     * @param list list object
     * @param targetClass final conversion class type
     * @throws ChangeValueException if any errors occurred on changing value in object
     * @throws InvalidArgumentException if arguments are null or invalid
     * @throws ResolutionException if resolution was failed
     */
    public void inject(final IObject object, final List list, final Class<? extends T> targetClass)
            throws ChangeValueException, InvalidArgumentException, ResolutionException {

        List<T> targetList;
        if (list.get(0).getClass().equals(targetClass)) {
            targetList = (List<T>) list;
        } else {
            targetList = new ArrayList<>(list.size());
            for (Object listItem : list) {
                T converted = IOC.resolve(Keys.getOrAdd(targetClass.toString()), listItem);
                targetList.add(converted);
            }
        }
        object.setValue(name, targetList);
    }

    /**
     * Delete from input {@link IObject} field {@link Field#name}
     * @param object input object
     * @throws DeleteValueException if any errors occurred on deleting value from object
     * @throws InvalidArgumentException if arguments null or invalid
     */
    public void delete(final IObject object) throws DeleteValueException, InvalidArgumentException {
        object.deleteField(name);
    }
}
