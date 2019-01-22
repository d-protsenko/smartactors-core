package info.smart_tools.smartactors.ioc_strategy_pack.resolve_by_type_and_name_strategy;

import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * There are to parameters: type and name.
 * There is single strategy for create instance by type.
 * At resolve method first parameter should be type and second name of the object instance
 */
public class ResolveByTypeAndNameStrategy implements IResolutionStrategy, IRegistrationStrategy {

    Map<String, IResolutionStrategy> creatingStrategy = new HashMap<>();
    Map<String, Object> createdDeserializationStrategies = new ConcurrentHashMap<>();

    @Override
    public Object resolve(final Object... args) throws ResolutionStrategyException {
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
    public void register(final Object key, final IResolutionStrategy strategy) throws RegistrationStrategyException {
        createdDeserializationStrategies.entrySet().removeIf((k -> k.getKey().startsWith((String) key)));
        creatingStrategy.put((String) key, strategy);
    }

    @Override
    public void unregister(final Object key) throws RegistrationStrategyException {
        creatingStrategy.remove(key);
    }
}