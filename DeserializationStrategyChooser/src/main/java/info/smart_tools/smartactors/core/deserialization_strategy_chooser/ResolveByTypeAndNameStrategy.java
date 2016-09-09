package info.smart_tools.smartactors.core.deserialization_strategy_chooser;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.core.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy for choosing {@link IDeserializeStrategy}
 * There are to parameters: type and name.
 * There is single strategy for create instance by type.
 * At resolve method first parameter should be type and second name of the object instance
 */
public class ResolveByTypeAndNameStrategy implements IResolveDependencyStrategy, IAdditionDependencyStrategy {

    Map<String, IResolveDependencyStrategy> creatingStrategy = new HashMap<>();
    Map<String, IDeserializeStrategy> createdDeserializationStrategies = new ConcurrentHashMap<>();

    @Override
    public IDeserializeStrategy resolve(final Object... args) throws ResolveDependencyStrategyException {
        //args[0] - type of the object
        //args[1] - name of the object
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