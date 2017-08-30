package info.smart_tools.smartactors.transformation_rules.get_first_not_null;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.Arrays;

public class GetFirstNotNullRule implements IResolveDependencyStrategy {

    /**
     * @param args array of needed parameters for resolve dependency
     * @param <T> target type of returned value
     * @return first param which will not null
     * @throws ResolveDependencyStrategyException
     */
    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
        for (Object arg : args) {
            if (arg != null) {
                return (T) arg;
            }
        }
        throw new ResolveDependencyStrategyException("All objects are null: " + Arrays.toString(args));
    }
}
