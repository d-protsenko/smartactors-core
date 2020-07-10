package info.smart_tools.smartactors.strategy.to_wrapper_converter_strategy;

import info.smart_tools.smartactors.base.interfaces.istrategy.IStrategy;
import info.smart_tools.smartactors.base.interfaces.istrategy.exception.StrategyException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;

/**
 * Implementation of {@link IStrategy}.
 * Convert given instance of {@link IObject}
 * to the specific wrapper
 */
public class ToWrapperConverter implements IStrategy {

    @Override
    public <T> T resolve(final Object ... args)
            throws StrategyException {

        try {
            IObject obj = (IObject) args[0];
            T instance = null;
            IWrapperGenerator wg = IOC.resolve(Keys.getKeyByName(IWrapperGenerator.class.getCanonicalName()));
            Object unknown = IOC.resolve(Keys.getKeyByName((String) args[1]));
            if (unknown.getClass().isInterface()) {
                instance = wg.generate((Class<T>) unknown);
            }
            ((IObjectWrapper) instance).init(obj);

            return instance;

        } catch (Throwable e) {
            throw new StrategyException(
                    "Could not convert given instance of IObject to the specific wrapper.",
                    e
            );
        }
    }
}
