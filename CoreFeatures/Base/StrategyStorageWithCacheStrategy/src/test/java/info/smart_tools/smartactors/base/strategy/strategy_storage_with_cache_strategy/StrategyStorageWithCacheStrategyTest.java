package info.smart_tools.smartactors.base.strategy.strategy_storage_with_cache_strategy;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiFunction;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.icacheable.ICacheable;
import info.smart_tools.smartactors.base.interfaces.icacheable.exception.DropCacheException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StrategyStorageWithCacheStrategyTest {

    @Before
    public void init()
            throws Exception {

    }

    @Test
    public void checkInstanceCreation()
            throws Exception {

        IResolveDependencyStrategy sscs = new StrategyStorageWithCacheStrategy(a -> a, (c, a) -> a);
        assertNotNull(sscs);
    }

    @Test
    public void checkResolution() throws Exception {
        Map<String, Integer> count = new HashMap<>();
        count.put("count", 0);
        IFunction argToKey = arg -> arg.getClass();
        IBiFunction findValueByArgument = (map, arg) -> {
            IResolveDependencyStrategy strategy = null;
            Integer tmp = count.get("count");
            tmp ++;
            count.put("count", tmp);
            for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                if (entry.getKey().isInstance(arg)) {
                    strategy = entry.getValue();

                    break;
                }
            }
            return strategy;
        };

        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
        IResolveDependencyStrategy intToString = new ApplyFunctionToArgumentsStrategy(
                args -> Integer.toString((Integer) args[0])
        );
        IResolveDependencyStrategy floatToString = new ApplyFunctionToArgumentsStrategy(
                args -> Float.toString((Float) args[0])
        );
        ((IAdditionDependencyStrategy)cachedStrategy).register(Integer.class, intToString);
        ((IAdditionDependencyStrategy)cachedStrategy).register(Float.class, floatToString);

        String result = cachedStrategy.resolve(1);
        assertEquals("1", result);
        result = cachedStrategy.resolve(1f);
        assertEquals("1.0", result);
        count.put("count", 0);
        result = cachedStrategy.resolve(2f);
        assertEquals("2.0", result);

        assertEquals((int) count.get("count"), 0);
    }

    @Test
    public void checkDropCache()
            throws Exception {
        Map<String, Integer> count = new HashMap<>();
        count.put("count", 0);
        IFunction argToKey = arg -> arg.getClass();
        IBiFunction findValueByArgument = (map, arg) -> {
            IResolveDependencyStrategy strategy = null;
            Integer tmp = count.get("count");
            tmp ++;
            count.put("count", tmp);
            for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                if (entry.getKey().isInstance(arg)) {
                    strategy = entry.getValue();

                    break;
                }
            }
            return strategy;
        };

        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
        IResolveDependencyStrategy floatToString = new ApplyFunctionToArgumentsStrategy(
                args -> Float.toString((Float) args[0])
        );
        ((IAdditionDependencyStrategy)cachedStrategy).register(Float.class, floatToString);

        String result = cachedStrategy.resolve(1f);
        assertEquals("1.0", result);
        ((ICacheable) cachedStrategy).dropCache();
        count.put("count", 0);
        result = cachedStrategy.resolve(2f);
        assertEquals("2.0", result);
        assertEquals((int) count.get("count"), 1);

        ((ICacheable) cachedStrategy).dropCacheFor(Float.class);
        count.put("count", 0);
        result = cachedStrategy.resolve(2f);
        assertEquals("2.0", result);
        assertEquals((int) count.get("count"), 1);
    }

    @Test (expected = ResolveDependencyStrategyException.class)
    public void checkOnRemove()
            throws Exception {
        IFunction argToKey = arg -> arg.getClass();
        IBiFunction findValueByArgument = (map, arg) -> {
            IResolveDependencyStrategy strategy = null;
            for (Map.Entry<Class, IResolveDependencyStrategy> entry : ((Map<Class, IResolveDependencyStrategy>) map).entrySet()) {
                if (entry.getKey().isInstance(arg)) {
                    strategy = entry.getValue();

                    break;
                }
            }
            return strategy;
        };

        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(argToKey, findValueByArgument);
        IResolveDependencyStrategy floatToString = new ApplyFunctionToArgumentsStrategy(
                args -> Float.toString((Float) args[0])
        );
        ((IAdditionDependencyStrategy)cachedStrategy).register(Float.class, floatToString);

        String result = cachedStrategy.resolve(2f);
        assertEquals("2.0", result);

        ((IAdditionDependencyStrategy) cachedStrategy).remove(Float.class);
        cachedStrategy.resolve(2f);
        fail();
    }

    @Test (expected = DropCacheException.class)
    public void checkExceptionOnDropCache()
            throws Exception {
        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(null, null);
        ((ICacheable) cachedStrategy).dropCacheFor(null);
    }

    @Test (expected = AdditionDependencyStrategyException.class)
    public void checkExceptionOnRegister()
            throws Exception {
        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(null, null);
        ((IAdditionDependencyStrategy) cachedStrategy).register(null, null);
    }

    @Test (expected = AdditionDependencyStrategyException.class)
    public void checkExceptionOnRemove()
            throws Exception {
        IResolveDependencyStrategy cachedStrategy = new StrategyStorageWithCacheStrategy(null, null);
        ((IAdditionDependencyStrategy) cachedStrategy).remove(null);
    }
}
