package info.smart_tools.smartactors.test.test_data_source_string_array;

import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link StringArrayDataSource}.
 */
public class StringArrayDataSourceTest {

    @Test
    public void checkCreation()
            throws Exception {
        ISource<String[], IObject> source = new StringArrayDataSource();
        assertNotNull(source);
    }

    @Test
    public void checkSetMethodAndIteration()
            throws Exception {
        String[] data = new String[] {
                "{\"a\": \"a\"}",
                "{\"a\": \"b\"}",
                "{\"a\": \"c\"}"
        };

        ISource<String[], IObject> source = new StringArrayDataSource();
        source.setSource(data);
        List<String> result = new ArrayList<>();
        for (IObject item : source) {
            result.add((String) item.getValue(new FieldName("a")));
        }
        assertEquals(result.get(0), "a");
        assertEquals(result.get(1), "b");
        assertEquals(result.get(2), "c");
    }

    @Test (expected = SourceExtractionException.class)
    public void checkSourceExtractionExceptionOnInvalidData()
            throws Exception {
        String[] data = new String[] {
                "{a}"
        };

        ISource<String[], IObject> source = new StringArrayDataSource();
        source.setSource(data);
        fail();
    }
}
