package info.smart_tools.smartactors.core.iobject;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * IObject interface
 */
public interface IObject {
    /**
     * Returns named field value from this object.
     *
     * @param name is name of field
     * @return named field value object
     *         or {@code null} if field does not exist
     * @throws ReadValueException in case of underlying exceptions
     *
     * @see FieldName
     */
    Object getValue(FieldName name) throws ReadValueException;

    /**
     * Sets new value for named field.
     * Or creates new field with given name and value.
     *
     * @param name is name of field
     * @param newValue is new value of field, it can be any Java object or {@code null}
     * @throws ChangeValueException in case of underlying exceptions
     *
     * @see FieldName
     */
    void setValue(FieldName name, Object newValue) throws ChangeValueException;

    /**
     * Delete object with note fieldname
     *
     * @param name is name of field.
     * @throws DeleteValueException if object couldn't delete value with note fieldname.
     */
    void deleteField(FieldName name) throws DeleteValueException;

    /**
     * Returns new iterator over set of fields of the object.
     *
     * @return an iterator.
     */
    IObjectIterator iterator();
}
