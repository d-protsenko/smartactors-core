package info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_actor.exception.JsonConnectionOptionsActorException;
import info.smart_tools.smartactors.database_postgresql_connection_options.json_connection_options_actor.wrapper.OptionsWrapper;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

public class JsonConnectionOptionsActor {
    public void register(final OptionsWrapper wrapper) throws JsonConnectionOptionsActorException {
        try {
            IOC.register(Keys.getOrAdd(wrapper.getConnectionOptionsRegistrationName()), new SingletonStrategy(
                    new ConnectionOptions() {
                        @Override
                        public String getUrl() throws ReadValueException {
                            return wrapper.getUrl();
                        }

                        @Override
                        public String getUsername() throws ReadValueException {
                            return wrapper.getUsername();
                        }

                        @Override
                        public String getPassword() throws ReadValueException {
                            return wrapper.getPassword();
                        }

                        @Override
                        public Integer getMaxConnections() throws ReadValueException {
                            return wrapper.getMaxConnections();
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
            ));
        } catch (InvalidArgumentException | ResolutionException | ReadValueException | RegistrationException e) {
            throw new JsonConnectionOptionsActorException("Can't configure JsonConnectionOptionsActor: " + e.getMessage(), e);
        }
    }
}
