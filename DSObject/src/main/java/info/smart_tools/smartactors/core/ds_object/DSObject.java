package info.smart_tools.smartactors.core.ds_object;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Json implementation of {@link IObject}
 */
public class DSObject implements IObject {

    private Map<IFieldName, Object> body;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SimpleModule module = new SimpleModule("Nested IObject serialization module");
        module.addSerializer(new StdSerializer<IObject>(IObject.class) {
            @Override
            public void serialize(
                    final IObject iObject,
                    final JsonGenerator jsonGenerator,
                    final SerializerProvider serializerProvider
            )
                    throws IOException {
                jsonGenerator.writeRawValue(iObject.toString());
            }
        });
        OBJECT_MAPPER.registerModule(module);
    }

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
            this.body = OBJECT_MAPPER.reader(new TypeReference<Map<FieldName, Object>>() { }).readValue(body);
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
    public <T> T serialize()
            throws SerializeException {
        try {
            return (T) OBJECT_MAPPER.writer().writeValueAsString(body);
        } catch (Throwable e) {
            throw new SerializeException();
        }
    }
}