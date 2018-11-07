package info.smart_tools.smartactors.plugin.null_connection_pool;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.pool.Pool;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;

/**
 * Plugin.
 * Implements {@link IPlugin}
 * Load connection pool of null objects.
 */
public class NullConnectionPoolPlugin implements IPlugin {

    /** Local storage for instance of {@link IBootstrap}*/
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * Constructor with single argument
     * @param bootstrap instance of {@link IBootstrap}
     * @throws InvalidArgumentException if any errors occurred
     */
    public NullConnectionPoolPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap)
            throws InvalidArgumentException {
        if (null == bootstrap) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IBootstrapItem<String> item = new BootstrapItem("PostgresConnectionPoolPlugin");
            item
//                .after("IOC")
                .process(() -> {
                    try {
                        IResolveDependencyStrategy poolStrategy = new ApplyFunctionToArgumentsStrategy(
                                (args) -> new Pool(1, NullConnection::new));
                        IKey databaseConnectionPoolKey = Keys.getOrAdd("DatabaseConnectionPool");
                        IOC.register(databaseConnectionPoolKey, poolStrategy);
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException("NullConnectionPool plugin can't load: can't get DatabaseConnectionPool key", e);
                    } catch (InvalidArgumentException e) {
                        throw new ActionExecuteException("NullConnectionPool plugin can't load: can't create strategy", e);
                    } catch (RegistrationException e) {
                        throw new ActionExecuteException("NullConnectionPool plugin can't load: can't register new strategy", e);
                    }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load null connection pool plugin", e);
        }
    }
}
