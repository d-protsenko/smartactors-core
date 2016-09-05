package info.smart_tools.smartactors.core.deserialization_strategy_chooser;

import info.smart_tools.smartactors.core.IDeserializeStrategy;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by sevenbits on 05.09.16.
 */
public class DeserializationStrategyChooser implements IResolveDependencyStrategy {

    Map<String, Function<Object[], IDeserializeStrategy>> creatingStrategy = new HashMap<>();
    Map<String, IDeserializeStrategy> createdDeserializationStrategies = new ConcurrentHashMap<>();

    public void register(final String key, final Function<Object[], IDeserializeStrategy> function) {
        creatingStrategy.put(key, function);
    }

    @Override
    public IDeserializeStrategy resolve(final Object... args) throws ResolveDependencyStrategyException {
        String keyForResolvingKey = (String) args[0];
        String key = keyForResolvingKey + args[1];
        if (!createdDeserializationStrategies.containsKey(key)) {
            createdDeserializationStrategies.put(key, creatingStrategy.get(key).apply(args));
        }
        return createdDeserializationStrategies.get(key);
    }
}