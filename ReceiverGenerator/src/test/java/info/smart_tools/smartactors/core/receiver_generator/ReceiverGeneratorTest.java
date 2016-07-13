package info.smart_tools.smartactors.core.receiver_generator;

import info.smart_tools.smartactors.core.class_generator_java_compile_api.ClassGenerator;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.field_name.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ireceiver_generator.IReceiverGenerator;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.core.string_ioc_key.Key;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReceiverGenerator}
 */
public class ReceiverGeneratorTest {

    @Before
    public void init() throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy(
                        (a) -> {
                            try {
                                return new Key((String) a[0]);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
        );
    }

    @Test
    public void checkCreation()
            throws Exception {
        CustomActor a = new CustomActor();
        IResolveDependencyStrategy strategy = mock(IResolveDependencyStrategy.class);
        IOC.register(Keys.getOrAdd("actorID"), strategy);
        when(strategy.resolve()).thenReturn(a);
        IObject configs = mock(IObject.class);
        IObject wrapperConfig = mock(IObject.class);
        when(configs.getValue(new FieldName("actor"))).thenReturn("actorID");
        when(configs.getValue(new FieldName("handler"))).thenReturn("doSomeWork");
        when(configs.getValue(new FieldName("wrapper"))).thenReturn(wrapperConfig);
        IReceiverGenerator rg = new ReceiverGenerator(null);
        rg.generate(configs);
    }
}
