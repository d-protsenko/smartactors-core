package info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;

/**
 * Convert from Map with string keys to IObject.
 */
public class StringToIObjectStrategy implements IStrategy {

    @Override
    public <T> T resolve(final Object... args) throws StrategyException {

        try {
            String jsonBody = String.valueOf(args[0]);
            return (T) new DSObject(jsonBody);
        } catch (Exception e) {
            throw new StrategyException("Can't create IObject from String.", e);
        }
    }
}
