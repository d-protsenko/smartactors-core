package info.smart_tools.smartactors.iobject.converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import info.smart_tools.smartactors.base.interfaces.transformation.ITransformable;
import info.smart_tools.smartactors.base.interfaces.transformation.exception.TransformationException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;

import java.io.IOException;
import java.util.Map;

public class MapStringTransformer implements ITransformable<Map<IFieldName, Object>, String> {

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
                try {

                    jsonGenerator.writeRawValue((String) iObject.serialize());
                } catch (SerializeException e) {
                    throw new IOException("Could not serialize DSObject.", e);
                }
            }
        });
        module.addDeserializer(Object.class, new ObjectDeserializer());
        OBJECT_MAPPER.registerModule(module);
    }

    @Override
    public String transformTo(Map<IFieldName, Object> obj) throws TransformationException {
        try {
            return OBJECT_MAPPER.writer().writeValueAsString(obj);
        } catch (Exception e) {
            throw new TransformationException("Could not transform given object.", e);
        }
    }

    @Override
    public Map<IFieldName, Object> transformFrom(String obj) throws TransformationException {
        try {
            return OBJECT_MAPPER.readerFor(new TypeReference<Map<FieldName, Object>>() {}).readValue(obj);
        } catch (Exception e) {
            throw new TransformationException("Could not transform given object.", e);
        }
    }
}


class ObjectDeserializer extends UntypedObjectDeserializer {

    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {
        try {
            if (JsonTokenId.ID_START_OBJECT == jp.getCurrentTokenId()) {
                return new DSObject(
                        (Map<IFieldName, Object>) jp.readValueAs(new TypeReference<Map<FieldName, Object>>() { })
                );
            }
        } catch (Exception e) {
            return super.deserialize(jp, ctxt);
        }
        return super.deserialize(jp, ctxt);
    }
}