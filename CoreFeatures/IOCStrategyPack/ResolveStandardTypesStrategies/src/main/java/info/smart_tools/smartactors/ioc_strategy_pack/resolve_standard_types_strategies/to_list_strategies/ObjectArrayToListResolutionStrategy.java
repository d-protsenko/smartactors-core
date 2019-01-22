package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.Arrays;

/**
 * Strategy for converting from array to list
 */
public class ObjectArrayToListResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            return (T) Arrays.asList(args);
        } catch (Exception e) {
            throw new ResolutionStrategyException("Can't create list from array.", e);
        }
    }
}
