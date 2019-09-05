package info.smart_tools.smartactors.field.nested_field;

import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NestedFieldTest {
    @Test
    public void Should_ReturnNestedFieldValue() throws Exception {
        IObject object = new DSObject("{\"fieldA\":{\"fieldB\":\"I am nested field\"}}");
        NestedField nestedField = new NestedField("fieldA/fieldB");

        assertEquals(nestedField.in(object, String.class), "I am nested field");
    }

    @Test
    public void Should_SetNestedFieldValue() throws Exception {
        IObject object = new DSObject("{\"fieldA\":{\"fieldB\":{}}}");

        NestedField nestedField = new NestedField("fieldA/fieldB/fieldC");

        nestedField.out(object, "FIELD");

        assertEquals(nestedField.in(object), "FIELD");
    }

    @Test
    public void Should_SetNestedFieldValue_when_field_is_not_set() throws Exception {
        IObject object = new DSObject("{\"fieldA\":{}}}");

        NestedField nestedField = new NestedField("fieldA/fieldB/fieldC");

        nestedField.out(object, "FIELD");

        assertEquals(nestedField.in(object), "FIELD");
    }
}
