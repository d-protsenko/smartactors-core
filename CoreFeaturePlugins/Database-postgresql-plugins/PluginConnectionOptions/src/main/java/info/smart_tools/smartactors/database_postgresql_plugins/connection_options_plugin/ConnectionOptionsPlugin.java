package info.smart_tools.smartactors.database_postgresql_plugins.connection_options_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;

import java.io.InputStream;
import java.util.Properties;

/**
 * Plugin for resolving connection options from properties
 */
public class ConnectionOptionsPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap the bootstrap
     */
    public ConnectionOptionsPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("ConnectionOptionsPlugin");

            item
                .after("IOC")
                .before("starter")
                .process(() -> {
                    try {
                        IOC.register(Keys.getOrAdd("PostgresConnectionOptions"), new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                Properties connectionProperties = new Properties();
                                ClassLoader loader = ConnectionOptionsPlugin.class.getClassLoader();
                                try (InputStream resourceStream = loader.getResourceAsStream("db_connection.properties")) {
                                    connectionProperties.load(resourceStream);
                                    return new ConnectionOptions() {

                                        @Override
                                        public String getUrl() throws ReadValueException {
                                            return connectionProperties.getProperty("url");
                                        }
                                        @Override
                                        public String getUsername() throws ReadValueException {
                                            return connectionProperties.getProperty("username");
                                        }
                                        @Override
                                        public String getPassword() throws ReadValueException {
                                            return connectionProperties.getProperty("password");
                                        }
                                        @Override
                                        public Integer getMaxConnections() throws ReadValueException {
                                            return Integer.parseInt(connectionProperties.getProperty("maxConnections", "1"));
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
                                    };
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        ));
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't get CreateSessionActor key", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't create strategy", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't register new strategy", e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load ConnectionOptionsPlugin", e);
        }
    }
}
