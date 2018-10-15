package info.smart_tools.smartactors.message_processing.wrapper_generator;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.ioc.string_ioc_key.Key;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.exception.WrapperGeneratorException;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link WrapperGenerator}
 */
public class WrapperGeneratorTest {

    @Before
    public void init()
            throws Exception {
        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope scope = ScopeProvider.getScope(keyOfMainScope);
        scope.setValue(IOC.getIocKey(), new StrategyContainer());
        ScopeProvider.setCurrentScope(scope);

        IOC.register(
                IOC.getKeyForKeyByNameResolveStrategy(),
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
    public void checkCreationAndUsageWrapperByInterface()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator();
        IWrapper w1 = wg.generate(IWrapper.class);
        assertNotNull(w1);
        // re-usage
        IWrapper w2 = wg.generate(IWrapper.class);
        assertNotNull(w2);
        assertNotSame(w1, w2);

        IInnerWrapper w3 = (IInnerWrapper)wg.generate(IInnerWrapper.class);
        assertNotNull(w3);
    }

    @Test (expected = WrapperGeneratorException.class)
    public void checkOnIncorrectInterfaceWithoutReadValueException()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator();
        wg.generate(IIncorrectWrapperWithoutReadValueException.class);
    }

    @Test (expected = WrapperGeneratorException.class)
    public void checkOnIncorrectInterfaceWithoutChangeValueException()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator();
        wg.generate(IIncorrectWrapperWithoutChangeValueException.class);
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTargetInterfaceNull()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator();
        wg.generate(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNotInterface()
            throws Exception {
        IWrapperGenerator wg = new WrapperGenerator();
        wg.generate(TestClass.class);
        fail();
    }

    @Test
    public void checkFastReturnOnSecondGenerationSameInterface()
            throws Exception {
    }
}

