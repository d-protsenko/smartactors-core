package info.smart_tools.smartactors.core.configuration_object;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;

import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of {@link @IObject}.
 * This implementation gets value on {@code getValue} method, leads it in to the canonical form and returns result.
 */
public class ConfigurationObject implements IObject {

    private IObject source;

    public ConfigurationObject(final IObject source)
            throws InvalidArgumentException {
        if (null == source) {
            throw new InvalidArgumentException("Argument should not be null.");
        }
        this.source = source;
    }

    @Override
    public Object getValue(final IFieldName name)
            throws ReadValueException, InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Name parameter should not be null.");
        }
        try {
            return IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), "resolve key for configuration object"),
                    this.source,
                    name
            );
        } catch (Throwable e) {
            throw new ReadValueException("Can't read value for current field name");
        }
    }

    @Override
    public void setValue(final IFieldName name, final Object value)
            throws ChangeValueException, InvalidArgumentException {
        throw new ChangeValueException("Method not implemented.");
    }

    @Override
    public void deleteField(final IFieldName name)
            throws DeleteValueException, InvalidArgumentException {
        throw new DeleteValueException("Method not implemented.");
    }

    @Override
    public <T> T serialize()
            throws SerializeException {
        throw new SerializeException("Method not implemented.");
    }

    @Override
    public Iterator<Map.Entry<IFieldName, Object>> iterator() {
        return null;
    }
}