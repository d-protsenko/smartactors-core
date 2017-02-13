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

        buffer = new StringBuffer("SELECT x from y where z ?? ? LIMIT ?;");
        assertEquals(2, AsyncQueryUtils.reformatBuffer(buffer));
        assertEquals("SELECT x from y where z ? $1 LIMIT $2;", buffer.toString());
    }
}
