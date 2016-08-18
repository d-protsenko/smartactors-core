package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.examples.db_collection.InMemoryDBCollectionServer;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import org.junit.Test;

/**
 * A set of examples to work with DB collections.
 */
public class DBCollectionExample {

    @Test
    public void testInMemoryDatabaseWorks() throws ServerInitializeException, ServerExecutionException {
        IServer server = new InMemoryDBCollectionServer();
        server.initialize();
        server.start();
    }

}
