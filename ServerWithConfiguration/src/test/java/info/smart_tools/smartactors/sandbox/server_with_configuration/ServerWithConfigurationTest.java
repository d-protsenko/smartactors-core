package info.smart_tools.smartactors.sandbox.server_with_configuration;

import org.junit.Test;

/**
 *
 */
public class ServerWithConfigurationTest {
    @Test
    public void checkServer()
            throws Exception {
        ServerWithConfiguration server = new ServerWithConfiguration();
         server.initialize();
        server.start();
    }
}
