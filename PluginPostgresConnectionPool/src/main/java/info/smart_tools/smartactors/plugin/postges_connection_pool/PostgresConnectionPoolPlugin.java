package info.smart_tools.smartactors.plugin.postges_connection_pool;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.istorage_connection.exception.StorageException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool.Pool;
import info.smart_tools.smartactors.core.postgres_connection.PostgresConnection;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

public class PostgresConnectionPoolPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public PostgresConnectionPoolPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IKey<Pool> postgresConnectionPoolKey = Keys.getOrAdd("PostgresConnectionPool");
            IBootstrapItem<String> item = new BootstrapItem("PostgresConnectionPoolPlugin");
            item.process(() -> {
                try {
                    IOC.register(postgresConnectionPoolKey, new CreateNewInstanceStrategy(
                            (args) -> {
                                ConnectionOptions connectionOptions = (ConnectionOptions) args[0];
                                if (connectionOptions == null) {
                                    throw new RuntimeException("Can't resolve connection pool: connectionOptions is null");
                                }

                                return new Pool(connectionOptions.getMaxConnections(), () -> {
                                    try {
                                        return new PostgresConnection(connectionOptions);
                                    } catch (StorageException e) {
                                        throw new RuntimeException(e);
                                    }
                                });

                            }));
                } catch (RegistrationException | InvalidArgumentException e) {
                    throw new RuntimeException(e);
                }
            });
            bootstrap.add(item);
        } catch (ResolutionException | InvalidArgumentException e) {
            throw new PluginException("Can't load postgres connection pool plugin", e);
        }
    }
}
