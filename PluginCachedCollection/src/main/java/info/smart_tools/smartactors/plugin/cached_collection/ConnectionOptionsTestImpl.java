package info.smart_tools.smartactors.plugin.cached_collection;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

public class ConnectionOptionsTestImpl implements ConnectionOptions {
    @Override
    public String getUrl() throws ReadValueException {
        return "jdbc:postgresql://localhost:5432/test_async";
    }

    @Override
    public String getUsername() throws ReadValueException {
        return "test_user";
    }

    @Override
    public String getPassword() throws ReadValueException {
        return "qwerty";
    }

    @Override
    public Integer getMaxConnections() throws ReadValueException {
        return 10;
    }

    @Override
    public void setUrl(String url) throws ChangeValueException {

    }

    @Override
    public void setUsername(String username) throws ChangeValueException {

    }

    @Override
    public void setPassword(String password) throws ChangeValueException {

    }

    @Override
    public void setMaxConnections(Integer maxConnections) throws ChangeValueException {

    }
}
