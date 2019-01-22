package info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;

/**
 * Convert from Map with string keys to IObject.
 */
public class StringToIObjectResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {

        try {
            String jsonBody = String.valueOf(args[0]);
            return (T) new DSObject(jsonBody);
        } catch (Exception e) {
            throw new ResolutionStrategyException("Can't create IObject from String.", e);
        }
    }
}
