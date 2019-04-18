package info.smart_tools.smartactors.iobject_extension.configuration_object;

import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CObjectStrategy implements IResolveDependencyStrategy, IAdditionDependencyStrategy {

    private final Map<Object, List<IResolveDependencyStrategy>> strategyStorage = new HashMap<>();

    @Override
    public <T> T resolve(Object... args)
            throws ResolveDependencyStrategyException {
        char[] symbols = args[0].toString().toCharArray();
        String defaultKey = "default";
        List<IResolveDependencyStrategy> strategies = null;
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
                result = ((IResolveDependencyStrategy) strategy).resolve(args);
                args[1] = result;
            }
        }

        return (T) result;
    }

    @Override
    public void register(Object arg, IResolveDependencyStrategy value)
            throws AdditionDependencyStrategyException {
        List strategies = this.strategyStorage.computeIfAbsent(arg, k -> new LinkedList());
        strategies.add(value);
    }

    @Override
    public void remove(Object arg)
            throws AdditionDependencyStrategyException {
    }
}
