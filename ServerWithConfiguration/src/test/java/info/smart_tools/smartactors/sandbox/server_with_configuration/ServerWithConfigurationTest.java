package info.smart_tools.smartactors.sandbox.server_with_configuration;

import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class ServerWithConfigurationTest {
    @Ignore
    @Test
    public void checkServer()
            throws Exception {
        ServerWithConfiguration server = new ServerWithConfiguration();
        server.initialize();
        server.start();
    }
}
