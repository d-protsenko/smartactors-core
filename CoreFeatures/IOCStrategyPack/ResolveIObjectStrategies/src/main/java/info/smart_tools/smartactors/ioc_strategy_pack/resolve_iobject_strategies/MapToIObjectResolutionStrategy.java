package info.smart_tools.smartactors.ioc_strategy_pack.resolve_iobject_strategies;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

import java.util.HashMap;
import java.util.Map;

/**
 * Convert from Map with string keys to IObject.
 */
public class MapToIObjectResolutionStrategy implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object... args) throws ResolutionStrategyException {

        try {
            Map<String, Object> stringObjectMap = (Map<String, Object>) args[0];
            Map<IFieldName, Object> fieldNameObjectMap = new HashMap<>();
            for (String key: stringObjectMap.keySet()) {
                IFieldName fieldName = IOC.resolve(Keys.resolveByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), key);
                fieldNameObjectMap.put(fieldName, stringObjectMap.get(key));
            }

            return (T) new DSObject(fieldNameObjectMap);
        } catch (Exception e) {
            throw new ResolutionStrategyException("Can't create IObject from Map.", e);
        }
    }
}
