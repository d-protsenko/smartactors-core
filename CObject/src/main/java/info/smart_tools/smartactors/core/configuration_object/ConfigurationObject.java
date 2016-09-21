package info.smart_tools.smartactors.core.configuration_object;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import info.smart_tools.smartactors.core.ioc.IOC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of {@link @IObject}.
 * This implementation gets value on {@code getValue} method, leads it in to the canonical form and returns result.
 */
public class ConfigurationObject implements IObject {

    //private IObject source;
    private Map<IFieldName, Object> body;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SimpleModule module = new SimpleModule("Nested IObject serialization module");
        module.addDeserializer(Object.class, new ObjectDeserializer());
        OBJECT_MAPPER.registerModule(module);
    }

    /**
     * Serialize incoming string and create new instance of {@link ConfigurationObject}
     * @param body incoming string data
     * @throws InvalidArgumentException if any errors occurred on object creation
     */
    public ConfigurationObject(final String body)
            throws InvalidArgumentException {
        try {
            this.body = OBJECT_MAPPER.reader(new TypeReference<Map<FieldName, Object>>() { }).readValue(body);
        } catch (Throwable e) {
            throw new InvalidArgumentException(e);
        }
    }

    /**
     * Create new instance of {@link ConfigurationObject} by given body of pairs {@link IFieldName}, {@link Object}
     * @param objectEntries map of pairs {@link IFieldName}, {@link Object}
     * @throws InvalidArgumentException if argument is null
     */
    public ConfigurationObject(final Map<IFieldName, Object> objectEntries)
            throws InvalidArgumentException {
        if (null == objectEntries) {
            throw new InvalidArgumentException("Argument should not be null.");
        }
        this.body = new HashMap<IFieldName, Object>(0);
        this.body.putAll((HashMap<IFieldName, Object>) objectEntries);
    }

    /**
     * Create empty instance of {@link ConfigurationObject}
     */
    public ConfigurationObject() {
        this.body = new HashMap<>(0);
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
                    body.get(name),
                    name
            );
        } catch (Throwable e) {
            throw new ReadValueException("Can't read value for current field name");
        }
    }

//    @Override
//    public void setValue(final IFieldName name, final Object value)
//            throws ChangeValueException, InvalidArgumentException {
//        throw new ChangeValueException("Method not implemented.");
//    }

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

/**
 * Custom deserializer.
 * Cast all nested json objects to {@link IObject}.
 */
class ObjectDeserializer extends UntypedObjectDeserializer {

    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {
        try {
            if (JsonTokenId.ID_START_OBJECT == jp.getCurrentTokenId()) {
                return new ConfigurationObject(
                        (Map<IFieldName, Object>) jp.readValueAs(new TypeReference<Map<FieldName, Object>>() { })
                );
            }
        } catch (Exception e) {
            return super.deserialize(jp, ctxt);
        }
        return super.deserialize(jp, ctxt);
    }
}