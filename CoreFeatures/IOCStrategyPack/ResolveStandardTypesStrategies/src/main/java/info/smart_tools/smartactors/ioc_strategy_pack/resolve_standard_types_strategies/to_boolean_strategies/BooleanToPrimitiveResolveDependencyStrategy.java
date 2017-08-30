package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_boolean_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Used to convert from Boolean to boolean.
 */
public class BooleanToPrimitiveResolveDependencyStrategy implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {

        Boolean value = args[0] == null ? false : (Boolean) args[0];
        return (T) value;
    }
}
