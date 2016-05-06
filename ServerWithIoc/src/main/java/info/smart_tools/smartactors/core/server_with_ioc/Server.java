package info.smart_tools.smartactors.core.server_with_ioc;

import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategyFactory;
import info.smart_tools.smartactors.core.ioc.IContainer;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ioc_container.Container;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.IScopeFactory;
import info.smart_tools.smartactors.core.iserver.exception.ServerExecutionException;
import info.smart_tools.smartactors.core.iserver.exception.ServerInitializeException;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.recursive_scope.ScopeFactory;
import info.smart_tools.smartactors.core.scope_provider.IScopeProviderContainer;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.scope_provider_container.ScopeProviderContainer;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategyFactory;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.strategy_provider.IStrategyProviderContainer;
import info.smart_tools.smartactors.core.strategy_provider.StrategyProvider;
import info.smart_tools.smartactors.core.strategy_provider_container.StrategyProviderContainer;
import info.smart_tools.smartactors.core.iserver.IServer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

import java.lang.reflect.Field;

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
            initializeIoc();
            initializeScopeProvider();
            initializeStrategyProvider();
            registerCreateNewInstanceStrategy();
            registerSingletonStrategy();
        } catch (Exception e) {
            throw new ServerInitializeException("Server Initialize failed.");
        }
    }

    public void start()
            throws ServerExecutionException {
        try {
            Key<String> key1 = new Key<String>(String.class, "a");
            IResolveDependencyStrategy strategy = StrategyProvider.createStrategy("singleton", "abcd");
            IOC.register(key1, strategy);
            String result1 = IOC.resolve(key1);

            Key<String> key2 = new Key<String>("b");
            Object[] args = new Object[]{"", "it.sevenbits.sandbox.bootstrap", "Func", "String", "byte[]", "int", "int"};
            strategy = StrategyProvider.createStrategy("create_new", args);
            IOC.register(key2, strategy);
            String result2 = IOC.resolve(key2, new byte[]{'a', 'b', 'c'}, 1, 2);


            System.out.println(result1 + "     -      " + result2);
        } catch (Exception e) {

        }
    }

    private void initializeScopeProvider()
            throws Exception {
        IScopeFactory scopeFactory = new ScopeFactory();
        IScopeProviderContainer scopeProviderContainer = new ScopeProviderContainer(scopeFactory);
        IStrategyContainer strategyContainer = new StrategyContainer();

        Field field = ScopeProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, scopeProviderContainer);
        field.setAccessible(false);

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        mainScope.setValue(IOC.getIocKey(), strategyContainer);
        ScopeProvider.setCurrentScope(mainScope);
    }

    private void initializeIoc()
            throws Exception {
        IContainer iocContainer = new Container();

        Field field = IOC.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, iocContainer);
        field.setAccessible(false);
    }

    private void initializeStrategyProvider()
            throws Exception {
        IStrategyProviderContainer strategyProviderContainer = new StrategyProviderContainer();

        Field field = StrategyProvider.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, strategyProviderContainer);
        field.setAccessible(false);
    }

    private void registerCreateNewInstanceStrategy()
            throws Exception {
        IStrategyFactory createNewInstanceStrategyFactory = new CreateNewInstanceStrategyFactory();
        StrategyProvider.addStrategyFactory("create_new", createNewInstanceStrategyFactory);
    }

    private void registerSingletonStrategy()
            throws Exception {
        IStrategyFactory singletonFactory = new SingletonStrategyFactory();
        StrategyProvider.addStrategyFactory("singleton", singletonFactory);
    }
}
