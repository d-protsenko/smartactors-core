package info.smart_tools.smartactors.database.postgresql_async.async_query_actor.impl;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for {@link AsyncQueryUtils}.
 */
public class AsyncQueryUtilsTest {
    @Test
    public void Should_reformatSQLBuffer()
            throws Exception {
        StringBuffer buffer;

        Object a1 = new Object();
        Object[] args = new Object[] {a1, 345};

        buffer = new StringBuffer("SELECT x from y where z ?? ? LIMIT ?;");
        assertEquals(2, AsyncQueryUtils.reformatBuffer(buffer, args));
        assertEquals("SELECT x from y where z ? $1::text LIMIT $2::numeric;", buffer.toString());
        assertEquals(a1.toString(), args[0]);
    }

    @Test
    public void Should_countParameterPlaceholders()
            throws Exception {
        StringBuffer buffer;

        buffer = new StringBuffer("SELECT x from y where z ?? ? LIMIT ?;");
        assertEquals(2, AsyncQueryUtils.countParams(buffer));
    }
}
