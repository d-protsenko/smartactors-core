package info.smart_tools.smartactors.test.test_data_source_string_array;

import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.test.isource.ISource;
import info.smart_tools.smartactors.test.isource.exception.SourceExtractionException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

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
    public void checkSetMethodAndCheckQueue()
            throws Exception {
        String[] data = new String[] {
                "{\"a\": \"a\"}",
                "{\"a\": \"b\"}",
                "{\"a\": \"c\"}"
        };

        ISource<String[], IObject> source = new StringArrayDataSource();
        source.setSource(data);
        BlockingDeque<IObject> queue = source.getQueue();
        assertNotNull(queue);
        assertEquals(queue.take().getValue(new FieldName("a")), "a");
        assertEquals(queue.take().getValue(new FieldName("a")), "b");
        assertEquals(queue.take().getValue(new FieldName("a")), "c");
    }

    @Test
    public void checkSourceExtractionOnDataWithBrokenElement()
            throws Exception {
        String[] data = new String[] {
                "{a}",
                "{\"a\": \"a\"}"
        };
        ISource<String[], IObject> source = new StringArrayDataSource();
        source.setSource(data);
        BlockingDeque<IObject> queue = source.getQueue();
        assertEquals(queue.take().getValue(new FieldName("a")), "a");
    }
}
