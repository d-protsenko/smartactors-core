package info.smart_tools.smartactors.plugin.compile_query;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.core.iplugin.IPlugin;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;
import info.smart_tools.smartactors.core.sql_commons.QueryStatementFactory;
import info.smart_tools.smartactors.core.sql_commons.exception.QueryStatementFactoryException;

import java.util.HashMap;
import java.util.Map;

/**
 * Plugin for load IOC-strategy for compiling queries
 */
public class CompileQueryPlugin implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    /**
     * @param bootstrap Target bootstrap for adding strategy
     */
    public CompileQueryPlugin(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            Map<QueryKey, CompiledQuery> queryMap = new HashMap<>();
            IBootstrapItem<String> item = new BootstrapItem("CompileQueryPlugin");
            item
                    .after("IOC")
                    .process(() -> {
                try {
                    IKey compiledQueryKey = Keys.getOrAdd(CompiledQuery.class.toString());
                    IOC.register(compiledQueryKey, new CreateNewInstanceStrategy(
                        (args) -> {
                            StorageConnection connection = (StorageConnection) args[0];
                            String task = String.valueOf(args[1]);
                            if (connection == null || task == null) {
                                throw new RuntimeException("Can't resolve compiled query: key parameter is null");
                            }
                            String id = connection.getId();
                            QueryKey queryKey = QueryKey.create(task, id);
                            CompiledQuery query = queryMap.get(queryKey);
                            if (query == null) {
                                QueryStatementFactory factory = (QueryStatementFactory) args[2];
                                if (factory == null) {
                                    throw new RuntimeException("Can't resolve compiled query: query statement is null");
                                }
                                try {
                                    QueryStatement queryStatement = factory.create();
                                    query = connection.compileQuery(queryStatement);
                                    queryMap.put(queryKey, query);
                                    //TODO:: how to remove old queries from map?
                                } catch (QueryStatementFactoryException | StorageException e) {
                                    throw new RuntimeException("Can't resolve compiled query: ", e);
                                }
                            }

                            return query;
                        }));
                } catch (ResolutionException e) {
                    throw new ActionExecuteException("CompileQuery plugin can't load: can't get CompileQuery key", e);
                } catch (InvalidArgumentException e) {
                    throw new ActionExecuteException("CompileQuery plugin can't load: can't create strategy", e);
                } catch (RegistrationException e) {
                    throw new ActionExecuteException("CompileQuery plugin can't load: can't register new strategy", e);
                }
            });
            bootstrap.add(item);
        } catch (InvalidArgumentException e) {
            throw new PluginException("Can't load compile query plugin", e);
        }
    }
}
