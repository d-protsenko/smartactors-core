package info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies;

import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

/**
 * Convert from Map with string keys to IObject.
 */
public class StringToIObjectResolveDependencyStrategy implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {

        try {
            String jsonBody = String.valueOf(args[0]);
            return (T) new DSObject(jsonBody);
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Can't create IObject from String.", e);
        }
    }
}
