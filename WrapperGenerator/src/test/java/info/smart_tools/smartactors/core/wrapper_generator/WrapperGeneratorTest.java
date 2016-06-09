package info.smart_tools.smartactors.core.wrapper_generator;


import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.iwrapper_generator.exception.WrapperGeneratorException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WrapperGenerator}
 */
public class WrapperGeneratorTest {

    private WrapperGenerator wrapperGenerator;

    public interface ISomeInterface extends IObjectWrapper {
        String getSomeStringValue();
        int getSomeIntValue();
        boolean isSomeBooleanValue();
        void setSomeStringValue(String val);
        void setSomeBooleanValue(boolean val);
        void setNotExistedValue(double val);
    }

    @Before
    public void init() {
        this.wrapperGenerator = new WrapperGenerator();
    }

    @Test
    public void Should_ReturnInstanceOfGeneratedClassByGivenInterface_When_InterfaceIsNotNull()
            throws WrapperGeneratorException, InvalidArgumentException {
        ISomeInterface inst = this.wrapperGenerator.generate(ISomeInterface.class);
        assertNotNull(inst);
        assertTrue(inst instanceof ISomeInterface);
        assertTrue(inst instanceof IObjectWrapper);
    }

    @Test (expected = InvalidArgumentException.class)
    public void Should_ThrowInvalidArgumentException_When_InterfaceIsNull()
            throws Exception {
        wrapperGenerator.generate(null);
    }

    @Test (expected = InvalidArgumentException.class)
    public void Should_ThrowIllegalArgumentException_When_GivenClassIsNotInterface()
            throws Exception {
        wrapperGenerator.generate(String.class);
    }

    @Test
    public void Should_HaveDeclaredPrivateFieldWithIObjectType_When_CreationSuccess()
            throws Exception {
        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        assertEquals(inst.getClass().getDeclaredField("wrapped").getType(), IObject.class);
    }

    @Test
    public void Should_HaveDeclaredPrivateFieldsWithFieldTypeForEachMethodWithoutDoubling_When_CreationSuccess()
            throws Exception {
        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        assertEquals(inst.getClass().getDeclaredField("_someStringValue").getType(), Field.class);
        assertEquals(inst.getClass().getDeclaredField("_someIntValue").getType(), Field.class);
        assertEquals(inst.getClass().getDeclaredField("_someBooleanValue").getType(), Field.class);
    }

    @Test
    public void Should_HaveAllMethodsDeclaredInInterface_When_CreationSuccess()
            throws Exception {
        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        Method[] interfaceDeclaredMethods = ISomeInterface.class.getDeclaredMethods();
        assertEquals(
                inst.getClass().getDeclaredMethods().length,
                interfaceDeclaredMethods.length + IObjectWrapper.class.getDeclaredMethods().length
        );
        for (Method m : interfaceDeclaredMethods) {
            assertNotNull(inst.getClass().getDeclaredMethod(m.getName(), m.getParameterTypes()));
        }
    }

    @Test
    public void Should_GettersReturnValuesFromInnerIObject_When_Called()
            throws Exception {
        IObject object = mock(IObject.class);
        when(object.getValue(new FieldName("someIntValue"))).thenReturn(1);
        when(object.getValue(new FieldName("someStringValue"))).thenReturn("string");
        when(object.getValue(new FieldName("someBooleanValue"))).thenReturn(true);
        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        inst.init(object);
        assertEquals(inst.getSomeIntValue(), object.getValue(new FieldName("someIntValue")));
        assertEquals(inst.getSomeStringValue(), object.getValue(new FieldName("someStringValue")));
        assertEquals(inst.isSomeBooleanValue(), object.getValue(new FieldName("someBooleanValue")));
    }

    @Test
    public void Should_ReturnInnerMessageFromWrappedMessage_When_Called()
            throws Exception {
        IObject object = mock(IObject.class);
        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        inst.init(object);
        assertEquals(inst.extractWrapped(), object);
    }

    @Test
    public void Should_SettersChangeOriginalIObjectValues_When_Called()
            throws Exception {
        IObject object = mock(IObject.class);

        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        inst.init(object);

        String newStringVal = "NONONO";
        when(object.getValue(new FieldName("someStringValue"))).thenReturn(newStringVal);
        when(object.getValue(new FieldName("someBooleanValue"))).thenReturn(false);
        doNothing().when(object).setValue(new FieldName("someStringValue"), newStringVal);
        doNothing().when(object).setValue(new FieldName("someBooleanValue"), false);
        inst.setSomeStringValue(newStringVal);
        inst.setSomeBooleanValue(false);

        assertEquals(inst.getSomeStringValue(), newStringVal);
        assertEquals(object.getValue(new FieldName("someStringValue")), newStringVal);
        verify(object, times(1)).setValue(new FieldName("someStringValue"), newStringVal);
        assertEquals(inst.isSomeBooleanValue(), false);
        assertEquals(object.getValue(new FieldName("someBooleanValue")), false);
        verify(object, times(1)).setValue(new FieldName("someBooleanValue"), false);
    }

    @Test
    public void Should_SettersCreateNewNonExistedFieldsInOriginalIObject_When_Called()
            throws Exception {
        IObject object = mock(IObject.class);

        ISomeInterface inst = wrapperGenerator.generate(ISomeInterface.class);
        inst.init(object);

        double notExistedVal = 12.42;
        when(object.getValue(new FieldName("notExistedValue"))).thenReturn(notExistedVal);
        doNothing().when(object).setValue(new FieldName("notExistedValue"), notExistedVal);

        inst.setNotExistedValue(notExistedVal);

        assertEquals(object.getValue(new FieldName("notExistedValue")), notExistedVal);
        verify(object, times(1)).setValue(new FieldName("notExistedValue"), notExistedVal);
    }

    public interface MyObject extends IObjectWrapper {

        IObject getList(int index);
        int countList();
        void setList(Iterable<IObject> it);
    }

    @Test
    public void shouldReturnCorrectCollectionSize_withWrapper()
            throws Exception
    {
        String json = "{\"list\":[{\"key1\": 1, \"key2\": \"str\"}, {\"key1\": \"2\", \"key2\": 3}]}";
        ArrayList array = new ArrayList<HashMap<IFieldName, Object>>();
        Map element1 = new HashMap<IFieldName, Object>(){{put(new FieldName("key1"), 1);put(new FieldName("key2"), "str");}};
        Map element2 = new HashMap<IFieldName, Object>(){{put(new FieldName("key1"), "2");put(new FieldName("key2"), 3);}};
        array.add(element1);
        array.add(element2);
        IObject object = mock(IObject.class);
        MyObject inst = wrapperGenerator.generate(MyObject.class);
        when(object.getValue(new FieldName("list"))).thenReturn(array);
        inst.init(object);

        assertEquals(inst.countList(), 2);
    }
//
//    @Test(dataProvider = "Should_SerializeIObjectWithoutAnyMaps_WhenItContainsInnerIObject",
//            dataProviderClass = DataProviderForIObjectTest.class)
//    public void shouldReturnCorrectCollectionItems_withWrapper(IObjectCreator creator)
//            throws GeneratorException, NoSuchMethodException, IllegalAccessException,
//            InvocationTargetException, InstantiationException, ReadValueException, ChangeValueException
//    {
//
//        String json = "{\"list\":[{\"key1\": 1, \"key2\": \"str\"}, {\"key1\": \"2\", \"key2\": \"3\"}]}";
//        Class<? extends MyObject> impl = wrapperGenerator.generateWrapperFor(MyObject.class);
//        MyObject var = impl.getDeclaredConstructor().newInstance();
//        var.init(creator.create(json));
//        IObject obj1 = var.getList(0);
//        IObject obj2 = var.getList(1);
//        Field<String> strField = new Field<>(new FieldName("key2"));
//        assertEquals(strField.from(obj1, String.class), "str");
//        assertEquals(strField.from(obj2, String.class), "3");
//    }
//
//    @Test(dataProvider = "Should_SerializeIObjectWithoutAnyMaps_WhenItContainsInnerIObject",
//            dataProviderClass = DataProviderForIObjectTest.class)
//    public void shouldCorrectSetCollections_withWrapper(IObjectCreator creator)
//            throws GeneratorException, NoSuchMethodException, IllegalAccessException,
//            InvocationTargetException, InstantiationException, ReadValueException, ChangeValueException
//    {
//        Class<? extends MyObject> impl = wrapperGenerator.generateWrapperFor(MyObject.class);
//        MyObject wrapper = impl.getDeclaredConstructor().newInstance();
//        wrapper.init(creator.createEmpty());
//        List<IObject> list = CollectionUtils.newArrayList(creator.create("{\"key\": 1}"), creator.create("{\"key\": 2}"));
//        wrapper.setList(list);
//        Field<Integer> intField = new Field<>(new FieldName("key"));
//        assertEquals(wrapper.countList(), 2);
//        assertTrue(intField.from(wrapper.getList(1), Integer.class).equals(2));
//    }
}
