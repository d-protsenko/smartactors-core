package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
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
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.anyOf;
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
        bootstrap.start();
    }

    @Test
    public void testFieldNameWithNew() throws InvalidArgumentException {
        IFieldName fieldName = new FieldName("name");
        assertEquals("name", fieldName.toString());
    }

    @Test
    public void testFieldNameFromIOC() throws ResolutionException {
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

}
