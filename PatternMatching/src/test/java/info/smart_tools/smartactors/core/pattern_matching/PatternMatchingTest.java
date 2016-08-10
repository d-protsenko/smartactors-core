package info.smart_tools.smartactors.core.pattern_matching;

import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.exception.PatternMatchingException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.imatcher.IMatcher;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests for PatternMatching
 */
public class PatternMatchingTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void match_EmptyPattern_ExceptionThrown()
            throws Exception {
        thrown.expect(PatternMatchingException.class);
        thrown.expectMessage("Pattern can not be empty. Pattern must contain several fields to compare.");
        PatternMatching pattern = new PatternMatching();
        pattern.match(new DSObject());
    }

    @Test
    public void match_NoTestObject_ExceptionThrown()
            throws Exception {
        thrown.expect(PatternMatchingException.class);
        thrown.expectMessage("Test object must be specified.");
        PatternMatching pattern = new PatternMatching();
        pattern.match(null);
    }

    @Test
    public void match_IncorrectPattern_ExceptionThrown()
            throws Exception {
        thrown.expect(InvalidArgumentException.class);
        PatternMatching pattern = new PatternMatching("some text");
        pattern.match(new DSObject());
    }

    @Test
    public void match_SamePatternAndTestObject_true()
            throws Exception {
        IMatcher pattern = new PatternMatching("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        IObject testObj = new DSObject("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        assertEquals(pattern.match(testObj), true);
    }

    @Test
    public void match_DifferentPatternAndTestObject_false()
            throws Exception {
        IMatcher pattern = new PatternMatching("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        IObject testObj = new DSObject("{\n" +
                "  \"other_value\": \"other_1\",\n" +
                "  \"other_string\": \"other_foo\"\n" +
                "}");
        assertEquals(pattern.match(testObj), false);
    }

    @Test
    public void match_ExtraFieldsInTestObject_true()
            throws Exception {
        IMatcher pattern = new PatternMatching("{\n" +
                "  \"value\": 1,\n" +
                "  \"string\": \"foo\"\n" +
                "}");
        IObject testObj = new DSObject("{\n" +
                "  \"value\": 1,\n" +
                "  \"other_value\": \"other_1\",\n" +
                "  \"string\": \"foo\",\n" +
                "  \"other_string\": \"other_foo\"\n" +
                "}");
        assertEquals(pattern.match(testObj), true);
    }

    @Test
    public void match_NullFieldValueContainedInTestObject_false()
            throws Exception {
        IFieldName fieldName = mock(IFieldName.class);
        Object obj = mock(Object.class);
        Map<IFieldName, Object> mapPattern = new HashMap<IFieldName, Object>(){{put(fieldName, obj);}};
        IMatcher pattern = new PatternMatching(mapPattern);
        Map<IFieldName, Object> mapTestObj = new HashMap<IFieldName, Object>(){{put(fieldName, null);}};
        IObject testObj = new DSObject(mapTestObj);
        assertEquals(pattern.match(testObj), false);
    }

    @Test
    public void match_NullFieldValuesContainedInPattern_ExceptionThrown()
            throws Exception {
        thrown.expect(PatternMatchingException.class);
        thrown.expectMessage("Pattern field value can not be null.");
        IFieldName fieldName = mock(IFieldName.class);
        Map<IFieldName, Object> mapPattern = new HashMap<IFieldName, Object>(){{put(fieldName, null);}};
        IMatcher pattern = new PatternMatching(mapPattern);
        Map<IFieldName, Object> mapTestObj = new HashMap<IFieldName, Object>(){{put(fieldName, null);}};
        IObject testObj = new DSObject(mapTestObj);
        assertEquals(pattern.match(testObj), true);
    }

}
