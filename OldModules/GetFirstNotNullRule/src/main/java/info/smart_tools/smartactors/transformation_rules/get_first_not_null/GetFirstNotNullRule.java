package info.smart_tools.smartactors.transformation_rules.get_first_not_null;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.Arrays;

public class GetFirstNotNullRule implements IResolutionStrategy {

    /**
     * @param args array of needed parameters for resolve dependency
     * @param <T> target type of returned value
     * @return first param which will not null
     * @throws ResolutionStrategyException
     */
    @Override
    public <T> T resolve(Object... args) throws ResolutionStrategyException {
        for (Object arg : args) {
            if (arg != null) {
                return (T) arg;
            }
        }
        throw new ResolutionStrategyException("All objects are null: " + Arrays.toString(args));
    }
}
