package info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface OptionsWrapper {
    String getConnectionOptionsRegistrationName() throws ReadValueException;

    String getUrl() throws ReadValueException;

    String getUsername() throws ReadValueException;

    String getPassword() throws ReadValueException;

    Integer getMaxConnections() throws ReadValueException;
}
