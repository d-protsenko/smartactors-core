package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_string_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

/**
 * from boolean primitive to string
 */
public class BooleanToStringResolutionStrategy implements IResolutionStrategy {
    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            return (T) String.valueOf((boolean) args[0]);
        } catch (Exception e) {
            throw new ResolutionStrategyException(e);
        }
    }
}
