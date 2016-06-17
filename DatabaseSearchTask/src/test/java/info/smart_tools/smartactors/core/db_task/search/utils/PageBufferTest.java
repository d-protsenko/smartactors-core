package info.smart_tools.smartactors.core.db_task.search.utils;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PageBufferTest {
    private IPageBuffer pageBuffer;

    @Before
    public void setUp() {
        pageBuffer = PageBuffer.create(3);
    }

    @Test
    public void saveInBufferOverMaxSizeTest() throws ReadValueException {
        List<IObject> objects1 = mock(List.class);
        List<IObject> objects2 = mock(List.class);
        List<IObject> objects3 = mock(List.class);
        List<IObject> objects4 = mock(List.class);

        when(objects1.size()).thenReturn(1);
        when(objects2.size()).thenReturn(2);
        when(objects3.size()).thenReturn(3);
        when(objects4.size()).thenReturn(4);

        pageBuffer.save(1, objects1);
        pageBuffer.save(2, objects2);
        pageBuffer.save(3, objects3);
        pageBuffer.save(4, objects4);

        assertEquals(pageBuffer.size(), 3);
        assertEquals(pageBuffer.get(1), null);
        assertEquals(pageBuffer.get(2).size(), 2);
        assertEquals(pageBuffer.get(3).size(), 3);
        assertEquals(pageBuffer.get(4).size(), 4);
    }
}
