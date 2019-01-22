package info.smart_tools.smartactors.base.strategy.strategy_storage_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunctionTwoArgs;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StrategyStorageStrategyTest {

    @Test
    public void checkInstanceCreation()
            throws Exception {
        IResolutionStrategy sss = new StrategyStorageStrategy((a) -> a, (c, a) -> a);
        assertNotNull(sss);
    }

    @Test
    public void checkResolution()
            throws Exception {
        String value1 = "result1";
        String value2 = "result2";
        String defaultValue = "default";
        IResolutionStrategy innerStrategy1 = mock(IResolutionStrategy.class);
        IResolutionStrategy innerStrategy2 = mock(IResolutionStrategy.class);
        IResolutionStrategy defaultStrategy = mock(IResolutionStrategy.class);
        when(innerStrategy1.resolve(new Object[] {"in_something", "arg"})).thenReturn(value1);
        when(innerStrategy2.resolve(new Object[] {"out_something", "arg"})).thenReturn(value2);
        when(defaultStrategy.resolve(new Object[] {"something", "arg"})).thenReturn(defaultValue);
        when(defaultStrategy.resolve(new Object[] {"in_something", "arg"})).thenReturn(defaultValue);

        IFunctionTwoArgs findValueByArgument = (map, arg) -> {
            char[] symbols = arg.toString().toCharArray();
            String defaultKey = "default";
            IResolutionStrategy strategy = null;
            StringBuilder key = new StringBuilder();
            for (char c : symbols) {
                key.append(c);
                strategy = ((Map<String, IResolutionStrategy>)map).get(key.toString());
                if (null != strategy) {
                    break;
                }
            }
            return null != strategy ? strategy : ((Map<String, IResolutionStrategy>)map).get(defaultKey);
        };
        IResolutionStrategy sss = new StrategyStorageStrategy((a) -> a, findValueByArgument);
        ((IRegistrationStrategy) sss).register("in_", innerStrategy1);
        ((IRegistrationStrategy) sss).register("out_", innerStrategy2);
        ((IRegistrationStrategy) sss).register("default", defaultStrategy);


        String result = sss.resolve("in_something", "arg");
        assertEquals(value1, result);
        result = sss.resolve("out_something", "arg");
        assertEquals(value2, result);
        result = sss.resolve("something", "arg");
        assertEquals(defaultValue, result);

        ((IRegistrationStrategy) sss).unregister("in_");

        result = sss.resolve("in_something", "arg");
        assertEquals(defaultValue, result);
    }

    @Test (expected = ResolutionStrategyException.class)
    public void checkResolutionException()
            throws Exception {
        IResolutionStrategy sss = new StrategyStorageStrategy(null, null);
        sss.resolve(null);
    }

    @Test (expected = RegistrationStrategyException.class)
    public void checkExceptionOnRegister()
            throws Exception {
        IResolutionStrategy strategy = new StrategyStorageStrategy(
                (a) -> {
                    throw new InvalidArgumentException("");
                },
                null
        );
        ((IRegistrationStrategy) strategy).register(null, null);
    }

    @Test (expected = RegistrationStrategyException.class)
    public void checkExceptionOnRemove()
            throws Exception {
        IResolutionStrategy strategy = new StrategyStorageStrategy(
                (a) -> {
                    throw new InvalidArgumentException("");
                },
                null
        );
        ((IRegistrationStrategy) strategy).unregister(null);
    }
}
