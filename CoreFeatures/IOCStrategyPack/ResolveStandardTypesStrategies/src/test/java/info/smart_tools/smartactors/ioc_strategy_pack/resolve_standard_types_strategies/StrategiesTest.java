package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.DoubleToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.FloatToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.IntegerToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal.StringToBigDecimalResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies.StringToDateResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.DoubleToIntResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_integer_strategies.StringToIntResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self.ClassToClassResolutionStrategy;
import info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies.ObjectToStringResolutionStrategy;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class StrategiesTest {
    @Test
    public void DoubleToBigDecimalTest() throws Exception {
        IResolutionStrategy strategy = new DoubleToBigDecimalResolutionStrategy();
        assertEquals(strategy.resolve(new Double("2")), new BigDecimal("2.0"));
    }

    @Test
    public void FloatToBigDecimalTest() throws Exception {
        IResolutionStrategy strategy = new FloatToBigDecimalResolutionStrategy();
        assertEquals(strategy.resolve(new Float("2")), new BigDecimal("2.0"));
    }

    @Test
    public void IntegerToBigDecimalTest() throws Exception {
        IResolutionStrategy strategy = new IntegerToBigDecimalResolutionStrategy();
        assertEquals(strategy.resolve(new Integer("2")), new BigDecimal("2"));
    }

    @Test
    public void StringToBigDecimalTest() throws Exception {
        IResolutionStrategy strategy = new StringToBigDecimalResolutionStrategy();
        assertEquals(strategy.resolve("2.0"), new BigDecimal("2.0"));
    }

    @Test
    public void StringToLocalDateTimeTest() throws Exception {
        IResolutionStrategy strategy = new StringToDateResolutionStrategy();
        assertEquals(strategy.resolve("2015-08-04T10:11:30"), LocalDateTime.parse("2015-08-04T10:11:30"));
    }

    @Test
    public void DoubleToIntegerTest() throws Exception {
        IResolutionStrategy strategy = new DoubleToIntResolutionStrategy();
        assertEquals(strategy.resolve(new Double("2.0")), Integer.valueOf("2"));
    }

    @Test
    public void StringToIntegerTest() throws Exception {
        IResolutionStrategy strategy = new StringToIntResolutionStrategy();
        assertEquals(strategy.resolve("2"), Integer.valueOf("2"));
    }

    @Test
    public void IntegerToString() throws Exception {
        IResolutionStrategy strategy = new ObjectToStringResolutionStrategy();
        assertEquals(strategy.resolve(new Double("2.0")), "2.0");
    }

    @Test
    public void ObjectToObject() throws Exception {
        Object object = new Object();
        IResolutionStrategy strategy = new ClassToClassResolutionStrategy();
        assertEquals(strategy.resolve(object), object);
    }
}
