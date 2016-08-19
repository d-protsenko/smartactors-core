package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field.Field;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.plugin.dsobject.PluginDSObject;
import info.smart_tools.smartactors.plugin.ifield.IFieldPlugin;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import info.smart_tools.smartactors.plugin.resolve_standard_types_strategies.ResolveStandardTypesStrategiesPlugin;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Samples how to use IObject.
 */
public class IObjectExample {

    @Before
    public void setUp() throws PluginException, ProcessExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        new IFieldPlugin(bootstrap).load();
        new PluginDSObject(bootstrap).load();
        new ResolveStandardTypesStrategiesPlugin(bootstrap).load();
        bootstrap.start();
    }

    @Test
    public void testFieldNameCreateNew() throws InvalidArgumentException {
        IFieldName fieldName = new FieldName("name");
        assertEquals("name", fieldName.toString());
    }

    @Test
    public void testFieldNameCreateIOC() throws ResolutionException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        assertEquals("name", fieldName.toString());
    }

    @Test
    public void testSetAndGet() throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        Object value = new Object();
        object.setValue(fieldName, value);
        assertSame(value, object.getValue(fieldName));
    }

    @Test
    public void testSetGetAndDelete() throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException, DeleteValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        Object value = new Object();
        object.setValue(fieldName, value);
        assertSame(value, object.getValue(fieldName));
        object.deleteField(fieldName);
        assertNull(object.getValue(fieldName));
    }

    @Test
    public void testIteration() throws ResolutionException, ChangeValueException, InvalidArgumentException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IFieldName firstFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "first");
        IFieldName secondFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "second");
        object.setValue(firstFieldName, "one");
        object.setValue(secondFieldName, "two");
        for (Map.Entry<IFieldName, Object> entry : object) {
            IFieldName fieldName = entry.getKey();
            Object value = entry.getValue();
            assertThat(fieldName, anyOf(is(firstFieldName), is(secondFieldName)));
            assertThat(value, anyOf(is("one"), is("two")));
        }
    }

    @Test
    public void testFieldCreateNew() throws ResolutionException, InvalidArgumentException, ChangeValueException, ReadValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        Object value = new Object();
        object.setValue(fieldName, value);

        IField field = new Field(fieldName);
        assertSame(value, field.in(object));
    }

    @Test
    public void testFieldCreateIOC() throws ResolutionException, InvalidArgumentException, ChangeValueException, ReadValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        Object value = new Object();
        object.setValue(fieldName, value);

        IField field = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "name");
        assertSame(value, field.in(object));
    }

    @Test
    public void testSetAndGetField() throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IField field = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "name");
        Object value = new Object();
        field.out(object, value);
        assertSame(value, field.in(object));
    }

    @Test
    public void testSetAndGetFieldTyped() throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException {
        IObject object = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IField field = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "name");
        String stringValue = "123";
        field.out(object, stringValue);
        Integer intValue = field.in(object, Integer.class);
        assertEquals(Integer.parseInt(stringValue), intValue.intValue());
    }

    @Test
    public void testNewEmptyDSObject() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        IObject object = new DSObject();
        assertNull(object.getValue(fieldName));
    }

    @Test
    public void testNewDSObjectFromJSON() throws ResolutionException, InvalidArgumentException, ReadValueException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        IObject object = new DSObject("{ \"name\": \"value\" }");
        assertEquals("value", object.getValue(fieldName));
    }

    @Test
    public void testNewDSObjectFromMap() throws ResolutionException, InvalidArgumentException, ReadValueException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        Map<IFieldName, Object> map = new HashMap<>();
        map.put(fieldName, "value");
        IObject object = new DSObject(map);
        assertEquals("value", object.getValue(fieldName));
    }

    @Test
    public void testResolveEmptyIObject() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        IObject object = IOC.resolve(
                Keys.getOrAdd(IObject.class.getCanonicalName()));
        assertNull(object.getValue(fieldName));
    }

    @Test
    public void testResolveIObjectFromJSON() throws ResolutionException, InvalidArgumentException, ReadValueException {
        IFieldName fieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        IObject object = IOC.resolve(
                Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{ \"name\": \"value\" }");
        assertEquals("value", object.getValue(fieldName));
    }

    @Test
    public void testJSONObjectField() throws ResolutionException, ReadValueException, InvalidArgumentException {
        IField field = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "name");
        IObject object = IOC.resolve(
                Keys.getOrAdd(IObject.class.getCanonicalName()),
                "{ \"name\": { \"nested\": \"object\" } }");
        IObject value = field.in(object);
        assertThat(value, is(instanceOf(IObject.class)));
        IField nestedField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "nested");
        assertEquals("object", nestedField.in(value));
    }

}
