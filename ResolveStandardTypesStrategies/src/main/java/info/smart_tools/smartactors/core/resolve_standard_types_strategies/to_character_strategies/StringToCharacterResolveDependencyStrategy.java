package info.smart_tools.smartactors.core.resolve_standard_types_strategies.to_character_strategies;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Converts from String to Character
 */
public class StringToCharacterResolveDependencyStrategy implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        try {
            return (T) new Character(String.valueOf(args[0]).charAt(0));
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException(e);
        }
    }
}
