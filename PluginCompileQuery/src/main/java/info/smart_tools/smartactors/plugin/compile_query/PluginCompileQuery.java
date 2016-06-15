package info.smart_tools.smartactors.plugin.compile_query;

import info.smart_tools.smartactors.core.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
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
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.util.HashMap;
import java.util.Map;

public class PluginCompileQuery implements IPlugin {

    private final IBootstrap<IBootstrapItem<String>> bootstrap;

    public PluginCompileQuery(final IBootstrap<IBootstrapItem<String>> bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void load() throws PluginException {

        try {
            //Note:: resolve by name strategy for keys should be defined
            IKey<CompiledQuery> compiledQueryKey = Keys.getOrAdd(CompiledQuery.class.toString());
            Map<String, CompiledQuery> queryMap = new HashMap<>();
            IBootstrapItem<String> item = new BootstrapItem("CompileQueryPlugin");
            item.process(() -> {
                try {
                    IOC.register(compiledQueryKey, new CreateNewInstanceStrategy(
                        (args) -> {
                            StorageConnection connection = (StorageConnection) args[0];
                            if (connection == null) {
                                throw new RuntimeException("Can't resolve compiled query: connection is null");
                            }
                            String id = connection.getId();
                            CompiledQuery query = queryMap.get(id);
                            if (query == null) {
                                QueryStatement queryStatement = (QueryStatement) args[1];
                                if (queryStatement == null) {
                                    throw new RuntimeException("Can't resolve compiled query: query statement is null");
                                }
                                try {
                                    query = connection.compileQuery(queryStatement);
                                    queryMap.put(id, query);
                                    //TODO:: how to remove old queries from map?
                                } catch (StorageException e) {
                                    throw new RuntimeException("Can't resolve compiled query: ", e);
                                }
                            }

                            return query;
                        }));
                } catch (RegistrationException | InvalidArgumentException e) {
                    e.printStackTrace();
                }
            });
            bootstrap.add(item);
        } catch (ResolutionException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
}
