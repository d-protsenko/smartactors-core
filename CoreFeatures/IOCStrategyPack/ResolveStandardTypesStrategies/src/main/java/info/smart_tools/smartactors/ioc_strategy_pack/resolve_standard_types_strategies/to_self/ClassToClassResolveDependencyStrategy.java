package info.smart_tools.smartactors.ioc_strategy_pack.resolve_standard_types_strategies.to_self;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Strategy for converting from class to itself
 */
public class ClassToClassResolveDependencyStrategy implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
            return (T) args[0];
    }
}
