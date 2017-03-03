package info.smart_tools.smartactors.core.examples.db_collection;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;

/**
 * Simplest hardcoded Postgres connection options.
 */
public class TestConnectionOptions implements ConnectionOptions {

    @Override
    public String getUrl() throws ReadValueException {
        return "jdbc:postgresql://localhost:5432/test";
    }

    @Override
    public String getUsername() throws ReadValueException {
        return "test";
    }

    @Override
    public String getPassword() throws ReadValueException {
        return "test";
    }

    @Override
    public Integer getMaxConnections() throws ReadValueException {
        return 1;
    }

    @Override
    public void setUrl(final String url) throws ChangeValueException {
    }

    @Override
    public void setUsername(final String username) throws ChangeValueException {
    }

    @Override
    public void setPassword(final String password) throws ChangeValueException {
    }

    @Override
    public void setMaxConnections(final Integer maxConnections) throws ChangeValueException {
    }

}
