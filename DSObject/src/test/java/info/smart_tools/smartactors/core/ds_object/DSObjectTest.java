package info.smart_tools.smartactors.core.ds_object;

import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.SerializeException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests for DSObject
 */
public class DSObjectTest {

    @Test
    public void checkCreationByEmptyConstructor() {
        IObject obj = new DSObject();
        assertNotNull(obj);
    }

    @Test
    public void checkCreationByString()
            throws Exception {
        IObject obj = new DSObject("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        assertNotNull(obj);
        assertEquals(1, obj.getValue(new FieldName("value")));
        assertEquals("foo", obj.getValue(new FieldName("string")));
    }

    @Test
    public void checkCreationByMap()
            throws Exception {
        IFieldName fieldName = mock(IFieldName.class);
        Object obj = mock(Object.class);
        Map<IFieldName, Object> map = new HashMap<IFieldName, Object>(){{put(fieldName, obj);}};
        IObject result = new DSObject(map);
        assertNotNull(obj);
        assertEquals(obj, result.getValue(fieldName));
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationByString()
            throws Exception {
        String str = null;
        new DSObject(str);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnCreationByMap()
            throws Exception {
        Map<IFieldName, Object> map = null;
        new DSObject(map);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnGetValue()
            throws Exception {
        (new DSObject()).getValue(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnSetValue()
            throws Exception {
        (new DSObject()).setValue(null, new Object());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkExceptionOnDeleteValue()
            throws Exception {
        (new DSObject()).deleteField(null);
        fail();
    }

    @Test
    public void checkSetGetAndDeleteValue()
            throws Exception {
        IFieldName fieldName = mock(IFieldName.class);
        Object value = mock(Object.class);
        IObject obj = new DSObject();
        assertNull(obj.getValue(fieldName));
        obj.setValue(fieldName, value);
        assertEquals(value, obj.getValue(fieldName));
        obj.deleteField(fieldName);
        assertNull(obj.getValue(fieldName));
    }

    @Test
    public void checkSerialization()
            throws Exception {
        String json = "{\"value\":1,\"string\":\"foo\"}";
        IObject obj = new DSObject(json);
        assertEquals(json, obj.serialize());
    }

    @Test (expected = SerializeException.class)
    public void checkExceptionOnSerialization()
            throws Exception {
        String json = "{\"value\":1,\"string\":\"foo\"}";
        IFieldName fieldName = mock(IFieldName.class);
        IObject obj = new DSObject(json);
        obj.setValue(fieldName, Thread.currentThread());
        obj.serialize();
        fail();
    }
}
