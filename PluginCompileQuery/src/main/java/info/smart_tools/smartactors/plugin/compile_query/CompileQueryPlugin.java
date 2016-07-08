package info.smart_tools.smartactors.plugin.compile_query;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.IStorageConnection;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

public class CompileQueryPlugin implements IPlugin {
    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public CompileQueryPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {
        try {
            IKey<ICompiledQuery> compiledQueryKey = Keys.getOrAdd(ICompiledQuery.class.toString());
            IBootstrapItem<String> item = new BootstrapItem("CompileQueryPlugin");
            item.process(() -> {
                try {
                    IOC.register(compiledQueryKey, new CreateNewInstanceStrategy(
                            (args) -> {
                                IStorageConnection connection = (IStorageConnection) args[0];
                                if (connection == null)
                                    throw new RuntimeException("Can't resolve compiled query: " +
                                            "connection parameter is null!");

                                QueryStatementFactory factory = (QueryStatementFactory) args[1];
                                if (factory == null)
                                    throw new RuntimeException("Can't resolve compiled query: " +
                                            "query statement factory parameter is null!");

                                try {
                                    return connection.compileQuery(factory.create());
                                } catch (QueryStatementFactoryException | StorageException e) {
                                    throw new RuntimeException("Can't resolve compiled query: ", e);
                                }
                            }));
                } catch (RegistrationException | InvalidArgumentException e) {
                    throw new RuntimeException(e);
                }
            });

            bootstrap.add(item);
        } catch (ResolutionException | InvalidArgumentException e) {
            throw new PluginException("Can't load compile query plugin", e);
        }
    }

}
