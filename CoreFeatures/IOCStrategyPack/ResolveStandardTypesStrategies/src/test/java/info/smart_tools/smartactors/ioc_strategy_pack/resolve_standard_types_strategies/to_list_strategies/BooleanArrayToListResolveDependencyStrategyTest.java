package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BooleanArrayToListResolutionStrategyTest {

    private BooleanArrayToListResolutionStrategy strategy;

    @Before
    public void setUp() {

        strategy = new BooleanArrayToListResolutionStrategy();
    }

    @Test
    public void ShouldConvertBooleanArrayToList() throws ResolutionStrategyException {

        boolean[] array = new boolean[] {true, false};
        List<Boolean> result = strategy.resolve(array);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), true);
        assertEquals(result.get(1), false);
    }

    @Test(expected = ResolutionStrategyException.class)
    public void ShouldThrowException_When_ErrorIsOccurred() throws ResolutionStrategyException {

        boolean var = false;
        strategy.resolve(var);
        fail();
    }
}
