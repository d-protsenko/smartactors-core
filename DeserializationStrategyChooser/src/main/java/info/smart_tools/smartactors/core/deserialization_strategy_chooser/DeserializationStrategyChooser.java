package info.smart_tools.smartactors.core.deserialization_strategy_chooser;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Strategy for choosing {@link IDeserializeStrategy}
 */
public class DeserializationStrategyChooser implements IResolveDependencyStrategy, IAdditionDependencyStrategy {

    Map<String, IResolveDependencyStrategy> creatingStrategy = new HashMap<>();
    Map<String, IDeserializeStrategy> createdDeserializationStrategies = new ConcurrentHashMap<>();

    @Override
    public IDeserializeStrategy resolve(final Object... args) throws ResolveDependencyStrategyException {
        //args[0] - type of the request
        //args[1] - name of the endpoint
        String keyForResolvingKey = (String) args[0];
        String key = keyForResolvingKey + args[1];
        if (!createdDeserializationStrategies.containsKey(key)) {
            createdDeserializationStrategies.put(key, creatingStrategy.get(keyForResolvingKey).resolve(args));
        }
        return createdDeserializationStrategies.get(key);
    }

    @Override
    public void register(final Object key, final IResolveDependencyStrategy strategy) throws AdditionDependencyStrategyException {
        creatingStrategy.put((String) key, strategy);
    }

    @Override
    public void remove(final Object key) throws AdditionDependencyStrategyException {
        creatingStrategy.remove(key);
    }
}