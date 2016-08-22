package info.smart_tools.smartactors.transformation_rules.object_to_list;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.Arrays;

public class ObjectToListRule implements IResolveDependencyStrategy {
    @Override
    public <T> T resolve(Object... args) throws ResolveDependencyStrategyException {
        return (T) Arrays.asList(args);
    }
}
