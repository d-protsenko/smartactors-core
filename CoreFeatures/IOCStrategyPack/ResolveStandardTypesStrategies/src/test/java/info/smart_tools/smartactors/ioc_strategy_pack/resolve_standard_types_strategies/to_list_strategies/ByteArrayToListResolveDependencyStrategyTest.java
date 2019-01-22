package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ByteArrayToListResolutionStrategyTest {

    private ByteArrayToListResolutionStrategy strategy;

    @Before
    public void setUp() {

        strategy = new ByteArrayToListResolutionStrategy();
    }

    @Test
    public void ShouldConvertByteArrayToList() throws ResolutionStrategyException {

        byte[] array = new byte[] {(byte)0xba, (byte)0x8a};
        List<Byte> result = strategy.resolve(array);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), new Byte((byte) 0xba));
        assertEquals(result.get(1), new Byte((byte) 0x8a));
    }

    @Test(expected = ResolutionStrategyException.class)
    public void ShouldThrowException_When_ErrorIsOccurred() throws ResolutionStrategyException {

        String invalid = "invalid";
        strategy.resolve(invalid);
        fail();
    }
}
