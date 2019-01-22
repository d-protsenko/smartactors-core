package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ObjectArrayToListResolutionStrategyTest {

    private ObjectArrayToListResolutionStrategy strategy;

    @Before
    public void setUp() {

        strategy = new ObjectArrayToListResolutionStrategy();
    }

    @Test
    public void ShouldConvertObjectArrayToList() throws ResolutionStrategyException {

        Object object = new Object();
        Object[] array = new Object[] {1L, "string", object};
        List<Short> result = strategy.resolve(array);
        assertEquals(result.size(), 3);
        assertEquals(result.get(0), new Long(1L));
        assertEquals(result.get(1), "string");
        assertEquals(result.get(2), object);
    }

    @Test
    public void ShouldConvertConcreteTypeArrayToList() throws ResolutionStrategyException {

        Integer[] array = new Integer[] {new Integer(1), new Integer(143)};
        List<Short> result = strategy.resolve(array);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), new Integer(1));
        assertEquals(result.get(1), new Integer(143));
    }

    @Test(expected = ResolutionStrategyException.class)
    public void ShouldThrowException_When_ErrorIsOccurred() throws ResolutionStrategyException {

        strategy.resolve(null);
        fail();
    }
}
