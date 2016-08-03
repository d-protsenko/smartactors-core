package info.smart_tools.smartactors.server.server_for_intern;

import org.junit.Test;

/**
 * Tests for
 */
public class ServerTest {

    @Test
    public void checkServer()
            throws Exception {
        Server server = new Server();
        server.initialize();
        server.start();
    }
}
