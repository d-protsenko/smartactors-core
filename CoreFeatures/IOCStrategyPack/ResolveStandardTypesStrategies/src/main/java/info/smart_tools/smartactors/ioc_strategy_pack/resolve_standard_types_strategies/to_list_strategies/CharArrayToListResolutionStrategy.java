package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for converting from array of char primitives to list of character objects
 */
public class CharArrayToListResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {

            char[] chars = (char[]) args[0];
            List<Character> result = new ArrayList<>(chars.length);
            for (char aChar : chars) {
                result.add(aChar);
            }

            return (T) result;
        } catch (Exception e) {
            throw new ResolutionStrategyException("Can't create list from char array.", e);
        }
    }
}
