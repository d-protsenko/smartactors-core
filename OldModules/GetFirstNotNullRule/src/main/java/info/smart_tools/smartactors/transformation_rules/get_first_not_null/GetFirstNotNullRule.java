package info.smart_tools.smartactors.transformation_rules.get_first_not_null;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;

import java.util.Arrays;

public class GetFirstNotNullRule implements IStrategy {

    /**
     * @param args array of needed parameters for resolve dependency
     * @param <T> target type of returned value
     * @return first param which will not null
     * @throws StrategyException
     */
    @Override
    public <T> T resolve(Object... args) throws StrategyException {
        for (Object arg : args) {
            if (arg != null) {
                return (T) arg;
            }
        }
        throw new StrategyException("All objects are null: " + Arrays.toString(args));
    }
}
