package info.smart_tools.smartactors.iobject_extension.configuration_object;

import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.IRegistrationStrategy;
import info.smart_tools.smartactors.base.interfaces.iregistration_strategy.exception.RegistrationStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CObjectStrategy implements IResolutionStrategy, IRegistrationStrategy {

    private final Map<Object, List<IResolutionStrategy>> strategyStorage = new HashMap<>();

    @Override
    public <T> T resolve(Object... args)
            throws ResolutionStrategyException {
        char[] symbols = args[0].toString().toCharArray();
        String defaultKey = "default";
        List<IResolutionStrategy> strategies = null;
        StringBuilder key = new StringBuilder();
        for (char c : symbols) {
            key.append(c);
            strategies = this.strategyStorage.get(key.toString());
            if (null != strategies) {
                break;
            }
        }
        Object result = null;
        if (null == strategies) {
            strategies = this.strategyStorage.get(defaultKey);
        }
        if (null != strategies) {
            for (Object strategy : strategies) {
                result = ((IResolutionStrategy) strategy).resolve(args);
                args[1] = result;
            }
        }

        return (T) result;
    }

    @Override
    public void register(Object arg, IResolutionStrategy value)
            throws RegistrationStrategyException {
        List strategies = this.strategyStorage.computeIfAbsent(arg, k -> new LinkedList());
        strategies.add(value);
    }

    @Override
    public void unregister(Object arg)
            throws RegistrationStrategyException {
    }
}
