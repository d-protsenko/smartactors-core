package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;

/**
 * from byte primitive to string
 */
public class ByteToStringStrategy implements IStrategy {
    @Override
    public <T> T resolve(final Object... args) throws StrategyException {
        try {
            return (T) String.valueOf((byte) args[0]);
        } catch (Exception e) {
            throw new StrategyException(e);
        }
    }
}
