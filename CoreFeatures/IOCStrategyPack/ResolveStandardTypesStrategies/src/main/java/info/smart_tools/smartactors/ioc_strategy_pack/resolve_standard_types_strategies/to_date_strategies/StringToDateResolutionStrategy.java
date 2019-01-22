package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_date_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.time.LocalDateTime;

/**
 * from String to LocalDateTime
 */
public class StringToDateResolutionStrategy implements IResolutionStrategy {
    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            return (T) LocalDateTime.parse((String) args[0]);
        } catch (Exception e) {
            throw new ResolutionStrategyException(e);
        }
    }
}
