package info.smart_tools.smartactors.core.server_with_features;

import info.smart_tools.smartactors.core.iserver.IServer;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by sevenbits on 10/4/16.
 */
public class ServerWithFeaturesTest {

    @Test
    @Ignore
    public void checkServer()
            throws Exception {
        IServer server = new ServerWithFeatures();
        server.initialize();
        server.start();
    }
}
