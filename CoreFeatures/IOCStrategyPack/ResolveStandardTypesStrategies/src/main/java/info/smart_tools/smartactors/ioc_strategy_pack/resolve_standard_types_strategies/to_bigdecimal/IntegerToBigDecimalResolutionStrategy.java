package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_bigdecimal;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.math.BigDecimal;
/**
 * from integer to bigdecimal
 */
public class IntegerToBigDecimalResolutionStrategy implements IResolutionStrategy {
    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            return (T) BigDecimal.valueOf((Integer) args[0]);
        } catch (Exception e) {
            throw new ResolutionStrategyException(e);
        }
    }
}
