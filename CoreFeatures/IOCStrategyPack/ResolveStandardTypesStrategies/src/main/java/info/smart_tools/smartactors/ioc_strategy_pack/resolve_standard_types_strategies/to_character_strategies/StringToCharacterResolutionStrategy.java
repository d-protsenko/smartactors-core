package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_character_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

/**
 * Converts from String to Character
 */
public class StringToCharacterResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {
            return (T) new Character(String.valueOf(args[0]).charAt(0));
        } catch (Exception e) {
            throw new ResolutionStrategyException(e);
        }
    }
}
