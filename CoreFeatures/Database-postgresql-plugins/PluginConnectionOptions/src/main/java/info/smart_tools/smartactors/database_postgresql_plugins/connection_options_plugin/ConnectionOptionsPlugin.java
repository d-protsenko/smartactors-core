package info.smart_tools.smartactors.database_postgresql_plugins.connection_options_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
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
            IField urlF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "url");
            IField usernameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "username");
            IField passwordF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "password");
            IField maxConnectionsF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "maxConnections");

            IBootstrapItem<String> item = new BootstrapItem("PostgresConnectionOptionsPlugin");
            item
                .process(() -> {
                    try {
                        IOC.register(Keys.getOrAdd("PostgresConnectionOptions"), new ApplyFunctionToArgumentsStrategy(
                            (args) -> {
                                IObject opts = (IObject) args[0];
                                return new ConnectionOptions() {

                                    @Override
                                    public String getUrl() throws ReadValueException {
                                        try {
                                            return urlF.in(opts);
                                        } catch (InvalidArgumentException e) {return null;}
                                    }
                                    @Override
                                    public String getUsername() throws ReadValueException {
                                        try {
                                            return usernameF.in(opts);
                                        } catch (InvalidArgumentException e) {return null;}
                                    }
                                    @Override
                                    public String getPassword() throws ReadValueException {
                                        try {
                                            return passwordF.in(opts);
                                        } catch (InvalidArgumentException e) {return null;}
                                    }
                                    @Override
                                    public Integer getMaxConnections() throws ReadValueException {
                                        try {
                                            return maxConnectionsF.in(opts);
                                        } catch (InvalidArgumentException e) {return 1;}
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
                            }
                        ));
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't get PostgresConnectionOptions key", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't create strategy", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("ConnectionOptions plugin can't load: can't register new strategy", e);
                    }
                });
            bootstrap.add(item);
        } catch (InvalidArgumentException | ResolutionException e) {
            throw new PluginException("Can't load PostgresConnectionOptionsPlugin", e);
        }
    }
}
