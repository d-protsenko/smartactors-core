package info.smart_tools.smartactors.core.server_with_ioc;

import info.smart_tools.actorsystem.core.scope_creation_event_handler.ScopeCreationEventHandler;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.create_new_instance_strategy.ExtendedStandardJavaFileManager;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

/**
 * Implementation {@link IServer} with scoped IOC
 */
public class Server implements IServer {

    /** Key for getting instance of {@link IStrategyContainer} from current scope */
    private static final String STRATEGY_CONTAINER_KEY = "strategy_container";

    /**
     * Initialize service locators
     * @throws ServerInitializeException if any errors occurred
     */
    public void initialize()
            throws ServerInitializeException {
        try {
            initializeScopeProvider();
        } catch (Exception e) {
            throw new ServerInitializeException("Server Initialize failed.");
        }
    }

    public void start()
            throws ServerExecutionException {
        try {
            Key<String> key1 = new Key<String>(String.class, "a");
            IResolveDependencyStrategy strategy = new SingletonStrategy("abcd");
            IOC.register(key1, strategy);
            String result1 = IOC.resolve(key1);

            Key<String> key2 = new Key<String>("b");
            Object[] args = new Object[]{"", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"};
            strategy = new CreateNewInstanceStrategy(args);
            IOC.register(key2, strategy);
            String result2 = IOC.resolve(key2, new byte[]{'a', 'b', 'c'}, 1, 2);

            IOC.register(IOC.getKeyForKeyStorage(), new ResolveByNameIocStrategy(
                    (a) -> new Key<IKey>((String) a[0]))
            );

            IKey key3 = IOC.resolve(IOC.getKeyForKeyStorage(), "key3");
            IKey key4 = IOC.resolve(IOC.getKeyForKeyStorage(), "key3");
            IKey key5 = IOC.resolve(IOC.getKeyForKeyStorage(), "key5");

            System.out.println(result1 + "     -      " + result2);
        } catch (Exception e) {

        } catch (Error er) {

        }
    }

    private void initializeScopeProvider()
            throws Exception {
            ScopeProvider.subscribeOnCreationNewScope(new ScopeCreationEventHandler(IOC.getIocKey()));
            Object keyOfMainScope = ScopeProvider.createScope(null);
            IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
            ScopeProvider.setCurrentScope(mainScope);
    }
}
