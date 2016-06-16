package info.smart_tools.smartactors.core.examples;

import info.smart_tools.smartactors.core.examples.scope.MyIOC;
import info.smart_tools.smartactors.core.examples.scope.MyIOCException;
import info.smart_tools.smartactors.core.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 *  Examples of using of the Scope.
 */
public class ScopeExample {

    private IScope systemScope;
    private IScope workerScope;

    @Before
    public void setUp() throws ScopeProviderException, InvalidArgumentException, RegistrationException, ScopeException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );
        MyIOC.init();   // must be initialized before any scope creation

        Object systemScopeKey = ScopeProvider.createScope(null);
        systemScope = ScopeProvider.getScope(systemScopeKey);
        ScopeProvider.setCurrentScope(systemScope); // unfortunately, the creation of the new scope now is sensible to the current scope
        Object workerScopeKey = ScopeProvider.createScope(systemScope);
        workerScope = ScopeProvider.getScope(workerScopeKey);

        IOC.register(IOC.getKeyForKeyStorage(), new ResolveByNameIocStrategy(
                (a) -> {
                    try {
                        return new Key((String) a[0]);
                    } catch (InvalidArgumentException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    @Test
    public void scopeIsKeyValueStorage() throws ScopeProviderException, InvalidArgumentException, ScopeException {
        Object scopeKey = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(scopeKey);
        Object key = new Key(java.util.UUID.randomUUID().toString());
        Object value = new Object();
        scope.setValue(key, value);
        assertSame(value, scope.getValue(key));
    }

    @Test
    public void nestedScope() throws ScopeProviderException, InvalidArgumentException, ScopeException {
        Object mainScopeKey = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(mainScopeKey);
        Object key = new Key(java.util.UUID.randomUUID().toString());
        ScopeProvider.setCurrentScope(mainScope);
        Object mainValue = new Object();
        IScope currentScope = ScopeProvider.getCurrentScope();
        assertSame(mainScope, currentScope);
        currentScope.setValue(key, mainValue);
        assertSame(mainValue, currentScope.getValue(key));

        Object nestedScopeKey = ScopeProvider.createScope(currentScope);
        IScope nestedScope = ScopeProvider.getScope(nestedScopeKey);
        ScopeProvider.setCurrentScope(nestedScope);
        currentScope = ScopeProvider.getCurrentScope();
        assertSame(nestedScope, currentScope);
        assertSame(mainValue, currentScope.getValue(key));
        Object nestedValue = new Object();
        currentScope.setValue(key, nestedValue);
        assertSame(nestedValue, currentScope.getValue(key));

        ScopeProvider.setCurrentScope(mainScope);
        currentScope = ScopeProvider.getCurrentScope();
        assertSame(mainScope, currentScope);
        assertSame(mainValue, currentScope.getValue(key));
    }

    @Test
    public void usageOfNestedScope() throws ScopeProviderException {
        assertSame(systemScope, ScopeProvider.getCurrentScope());
        ScopeProvider.setCurrentScope(workerScope);
        workerCall();
        ScopeProvider.setCurrentScope(systemScope);
        assertSame(systemScope, ScopeProvider.getCurrentScope());
    }

    private void workerCall() throws ScopeProviderException {
        assertSame(workerScope, ScopeProvider.getCurrentScope());
        // worker code
        systemCall();
        // worker code
        assertSame(workerScope, ScopeProvider.getCurrentScope());
    }

    private void systemCall() throws ScopeProviderException {
        IScope returnScope = ScopeProvider.getCurrentScope();
        ScopeProvider.setCurrentScope(systemScope);
        assertSame(systemScope, ScopeProvider.getCurrentScope());
        // system code
        ScopeProvider.setCurrentScope(returnScope);
    }

    @Test
    public void sampleIOC() throws InvalidArgumentException, MyIOCException, ScopeProviderException {
        assertSame(systemScope, ScopeProvider.getCurrentScope());

        IKey key = new Key<MyClass>("my");
        MyClass main = new MyClass("main");
        MyIOC.register(key, main);
        assertEquals(main, MyIOC.resolve(key));

        ScopeProvider.setCurrentScope(workerScope);
        assertEquals(main, MyIOC.resolve(key));
        MyClass worker = new MyClass("worker");
        MyIOC.register(key, worker);
        assertEquals(worker, MyIOC.resolve(key));

        ScopeProvider.setCurrentScope(systemScope);
        assertEquals(main, MyIOC.resolve(key));
    }

    @Test
    public void testSystemIOC() throws ScopeProviderException, ResolutionException, InvalidArgumentException, RegistrationException {
        assertSame(systemScope, ScopeProvider.getCurrentScope());
        IKey<MyClass> key = Keys.getOrAdd("test");
        MyClass systemObject = new MyClass("system");
        IOC.register(key, new SingletonStrategy(systemObject));
        assertEquals(systemObject, IOC.resolve(key));

        ScopeProvider.setCurrentScope(workerScope);
        assertEquals(systemObject, IOC.resolve(key));
        MyClass workerObject = new MyClass("worker");
        IOC.register(key, new SingletonStrategy(workerObject));
        assertEquals(workerObject, IOC.resolve(key));

        ScopeProvider.setCurrentScope(systemScope);
        assertEquals(systemObject, IOC.resolve(key));
    }

}
