package info.smart_tools.smartactors.plugin.create_session;

import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ipool.IPool;

//TODO:: remove this class. It has been added only for testing purposes
public class CreateSessionConfigImpl implements CreateSessionConfig {

    private IPool connectionPool;

    public CreateSessionConfigImpl(final IPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public String getCollectionName() throws ReadValueException {
        return "session";
    }

    @Override
    public IPool getConnectionPool() throws ReadValueException {
        return connectionPool;
    }
}
