package info.smart_tools.smartactors.core.iobject;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;

/**
 * IObject interface
 */
public interface IObject {
    /**
     * Returns named field value from this object.
     * @param name is name of field
     * @return named field value object
     *         or {@code null} if field does not exist
     * @throws ReadValueException in case of underlying exceptions
     * @throws InvalidArgumentException if incoming argument is null
     *
     * @see IFieldName
     */
    Object getValue(IFieldName name) throws ReadValueException, InvalidArgumentException;

    /**
     * Sets new value for named field.
     * Or creates new field with given name and value.
     *
     * @param name is name of field
     * @param value is new value of field, it can be any Java object or {@code null}
     * @throws ChangeValueException in case of underlying exceptions
     * @throws InvalidArgumentException if incoming argument is null
     *
     * @see IFieldName
     */
    void setValue(IFieldName name, Object value) throws ChangeValueException, InvalidArgumentException;

    /**
     * Delete object with note fieldname
     *
     * @param name is name of field.
     * @throws DeleteValueException if object couldn't delete value with note fieldname.
     * @throws InvalidArgumentException if incoming argument is null
     */
    void deleteField(IFieldName name) throws DeleteValueException, InvalidArgumentException;

    /**
     * Serialize instance of {@link IObject} to
     * @param <T> type of serialized instance of {@link IObject}
     * @return serialized instance of {@link IObject}
     * @throws SerializeException if any errors occurred on serialization
     */
    <T> T serialize() throws SerializeException;
}
