package info.smart_tools.smartactors.core.response_sender_chooser;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.iresponse_sender.IResponseSender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Strategy for choosing {@link IResponseSender}
 */
public class ResponseSenderChooser implements IResolveDependencyStrategy {
    Map<String, Function<Object[], IResponseSender>> creatingStrategy = new HashMap<>();
    Map<String, IResponseSender> createdDeserializationStrategies = new ConcurrentHashMap<>();

    public void register(final String key, final Function<Object[], IResponseSender> function) {
        creatingStrategy.put(key, function);
    }

    @Override
    public IResponseSender resolve(final Object... args) throws ResolveDependencyStrategyException {
        String keyForResolvingKey = (String) args[0];
        String key = keyForResolvingKey + args[1];
        if (!createdDeserializationStrategies.containsKey(key)) {
            createdDeserializationStrategies.put(key, creatingStrategy.get(key).apply(args));
        }
        return createdDeserializationStrategies.get(key);
    }
}
