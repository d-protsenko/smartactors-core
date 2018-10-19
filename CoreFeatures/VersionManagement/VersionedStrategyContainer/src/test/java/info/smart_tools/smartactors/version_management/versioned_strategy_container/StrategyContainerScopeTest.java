package info.smart_tools.smartactors.version_management.versioned_strategy_container;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 *  Test how IoC with this strategy container works in complex, with nested scope.
 */
public class StrategyContainerScopeTest {

    private IScope parentScope;
    private IScope childScope;
    private IResolveDependencyStrategy resolveByNameStrategy = mock(IResolveDependencyStrategy.class);

    @Before
    public void setUp() throws ScopeProviderException, InvalidArgumentException, RegistrationException {
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object parentScopeKey = ScopeProvider.createScope(null);
        parentScope = ScopeProvider.getScope(parentScopeKey);
        Object childScopeKey = ScopeProvider.createScope(parentScope);
        childScope = ScopeProvider.getScope(childScopeKey);

        ScopeProvider.setCurrentScope(parentScope);
        IOC.register(IOC.getKeyForKeyByNameResolveStrategy(), new ResolveByNameIocStrategy(
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
    public void testSystemIOC() throws ScopeProviderException, ResolutionException, InvalidArgumentException, RegistrationException, DeletionException {
        IKey key = Keys.getOrAdd("test");

        ScopeProvider.setCurrentScope(parentScope);
        try {
            IOC.resolve(key);
            fail();
        } catch (ResolutionException e) {
            // no strategy
        }
        ScopeProvider.setCurrentScope(childScope);
        try {
            IOC.resolve(key);
            fail();
        } catch (ResolutionException e) {
            // no strategy
        }

        ScopeProvider.setCurrentScope(parentScope);
        Object parentObject = new Object();
        IOC.register(key, new SingletonStrategy(parentObject));

        ScopeProvider.setCurrentScope(parentScope);
        assertSame(parentObject, IOC.resolve(key));
        ScopeProvider.setCurrentScope(childScope);
        assertSame(parentObject, IOC.resolve(key));

        ScopeProvider.setCurrentScope(childScope);
        Object childObject = new Object();
        IOC.register(key, new SingletonStrategy(childObject));

        ScopeProvider.setCurrentScope(parentScope);
        assertSame(parentObject, IOC.resolve(key));
        ScopeProvider.setCurrentScope(childScope);
        assertSame(childObject, IOC.resolve(key));

        ScopeProvider.setCurrentScope(childScope);
        IOC.remove(key);

        ScopeProvider.setCurrentScope(parentScope);
        assertSame(parentObject, IOC.resolve(key));
        ScopeProvider.setCurrentScope(childScope);
        assertSame(parentObject, IOC.resolve(key));

        ScopeProvider.setCurrentScope(parentScope);
        IOC.remove(key);

        ScopeProvider.setCurrentScope(parentScope);
        try {
            IOC.resolve(key);
            fail();
        } catch (ResolutionException e) {
            // no strategy
        }
        ScopeProvider.setCurrentScope(childScope);
        try {
            IOC.resolve(key);
            fail();
        } catch (ResolutionException e) {
            // no strategy
        }
    }

}
