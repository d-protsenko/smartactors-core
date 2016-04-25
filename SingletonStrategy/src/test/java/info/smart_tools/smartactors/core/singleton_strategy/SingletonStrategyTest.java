package info.smart_tools.smartactors.core.singleton_strategy;

import info.smart_tools.smartactors.core.class_storage.ClassStorage;
import info.smart_tools.smartactors.core.class_storage.exception.ClassStorageException;
import info.smart_tools.smartactors.core.class_storage_container.ClassStorageContainer;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IStrategyFactory;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.StrategyFactoryException;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link SingletonStrategy}
 */
public class SingletonStrategyTest {

    @Test
    public void checkStrategyResolution()
            throws ClassStorageException, StrategyFactoryException, NoSuchFieldException, IllegalAccessException,
            ResolveDependencyStrategyException {
        IStrategyFactory factory = new SingletonStrategyFactory();

        Field field = ClassStorage.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(null, new ClassStorageContainer());

        ClassStorage.addClass("str", String.class);
        ClassStorage.addClass("long_class_id", Long.class);
        ClassStorage.addClass("int_class_id", int.class);

        Object[] params = new Object[] {"byte[]", new byte[]{'a', 'b', 'c'}, "int", 1, "int", 2};
        Object[] args = new Object[] {"str", params};
        IResolveDependencyStrategy strategy1 = factory.createStrategy(args);
        String resultString = strategy1.resolve(null);
        assertEquals("bc", resultString);

        params = new Object[] {"long", 2};
        args = new Object[] {"long_class_id", params};
        IResolveDependencyStrategy strategy2 = factory.createStrategy(args);
        assertNotNull(strategy2);
        Long resultLong = strategy2.resolve(null);
        assertEquals((long) 2, (long) resultLong);

        params = new Object[] {"byte[]", new byte[]{'a', 'b', 'c'}, "int_class_id", 1, "int_class_id", 2};
        args = new Object[] {"str", params};
        IResolveDependencyStrategy strategy3 = factory.createStrategy(args);
        assertNotNull(strategy3);
        String resultStringExtended = strategy3.resolve(null);
        assertEquals("bc", resultStringExtended);
    }

}
