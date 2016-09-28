package info.smart_tools.smartactors.plugin.async_ops_collection;

import info.smart_tools.smartactors.core.async_operation_collection.AsyncOperationCollection;
import info.smart_tools.smartactors.core.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_composite_name_ioc_with_lambda_strategy.ResolveByCompositeNameIOCStrategy;

/**
 * Plugin for registration strategy of create async ops collection with IOC.
 * IOC resolve method waits collectionName as a first parameter.
 */
public class AsyncOpsCollectionPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor
     * @param bootstrap bootstrap
     */
    public AsyncOpsCollectionPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            IBootstrapItem<String> item = new BootstrapItem("AsyncOpsCollectionPlugin");

            item
                    .after("IOC")
                    .before("starter")
                    .process(() -> {
                        try {
                            IKey asyncCollectionKey = Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName());
                            IOC.register(asyncCollectionKey, new ResolveByCompositeNameIOCStrategy(
                                    (args) -> {
                                        try {
                                            String collectionName = String.valueOf(args[0]);
                                            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
                                            IPool connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);

                                            return new AsyncOperationCollection(connectionPool, collectionName);
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }));
                        } catch (RegistrationException | InvalidArgumentException | ResolutionException e) {
                            throw new ActionExecuteException("Error during registration strategy for collection.", e);
                        }
                    });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load AsyncOpsCollectionPlugin plugin", e);
        }
    }
}
