package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_list_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for converting from array of float primitives to list of float objects
 */
public class FloatArrayToListResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {
        try {

            float[] floats = (float[]) args[0];
            List<Float> result = new ArrayList<>(floats.length);
            for (float aFloat : floats) {
                result.add(aFloat);
            }

            return (T) result;
        } catch (Exception e) {
            throw new ResolutionStrategyException("Can't create list from float array.", e);
        }
    }
}
