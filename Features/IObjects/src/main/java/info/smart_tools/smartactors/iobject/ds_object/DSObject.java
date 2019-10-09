package info.smart_tools.smartactors.iobject.ds_object;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.transformation.ITransformable;
import info.smart_tools.smartactors.iobject.converter.MapStringTransformer;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Json implementation of {@link IObject}
 */
public class DSObject implements IObject {

    private Map<IFieldName, Object> body;
    private static ITransformable transformer = new MapStringTransformer();

    /**
     * Create new instance of {@link DSObject} by given body of pairs {@link IFieldName}, {@link Object}
     * @param objectEntries map of pairs {@link IFieldName}, {@link Object}
     * @throws InvalidArgumentException if argument is null
     */
    public DSObject(final Map<IFieldName, Object> objectEntries)
            throws InvalidArgumentException {
        if (null == objectEntries) {
            throw new InvalidArgumentException("Argument should not be null.");
        }
        this.body = new HashMap<>(0);
        this.body.putAll(objectEntries);
    }

    /**
     * Serialize incoming string and create new instance of {@link DSObject}
     * @param body incoming string data
     * @throws InvalidArgumentException if any errors occurred on object creation
     */
    public DSObject(final String body)
            throws InvalidArgumentException {
        try {
            this.body = (Map<IFieldName, Object>) transformer.transformFrom(body);
        } catch (Throwable e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Create empty instance of {@link DSObject}
     */
    public DSObject() {
        this.body = new HashMap<>(0);
    }

    @Override
    public Object getValue(final IFieldName name)
            throws ReadValueException, InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Name parameter should not be null.");
        }
        return body.get(name);
    }

    @Override
    public void setValue(final IFieldName name, final Object value)
            throws ChangeValueException, InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Name parameter should not be null.");
        }
        body.put(name, value);
    }

    @Override
    public void deleteField(final IFieldName name)
            throws DeleteValueException, InvalidArgumentException {
        if (null == name) {
            throw new InvalidArgumentException("Name parameter should not be null.");
        }
        body.remove(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T serialize()
            throws SerializeException {
        try {
            return (T) transformer.transformTo(body);
        } catch (Throwable e) {
            throw new SerializeException();
        }
    }

    @Override
    public Iterator<Map.Entry<IFieldName, Object>> iterator() {
        return new DSObjectIterator();
    }

    /**
     * Iterator over {@code body}
     */
    private final class DSObjectIterator implements Iterator<Map.Entry<IFieldName, Object>> {

        private Iterator<Map.Entry<IFieldName, Object>> iterator;

        private DSObjectIterator() {
            this.iterator = body.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public Map.Entry<IFieldName, Object> next() {
            return this.iterator.next();
        }
    } 
}