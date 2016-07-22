package info.smart_tools.smartactors.core.resolve_object_from_list_strategy;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.List;

public class ResolveObjectFromListDependencyStrategy implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {

        try {
            List list = (List) args[0];
            Integer objectIndex = (Integer) args[1];

            return (T) list.get(objectIndex);
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Can't create IObject from Map.", e);
        }
    }
}
