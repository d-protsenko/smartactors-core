package info.smart_tools.smartactors.strategy.to_wrapper_converter_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;

/**
 * Implementation of {@link IResolutionStrategy}.
 * Convert given instance of {@link IObject}
 * to the specific wrapper
 */
public class ToWrapperConverter implements IResolutionStrategy {

    @Override
    public <T> T resolve(final Object ... args)
            throws ResolutionStrategyException {

        try {
            IObject obj = (IObject) args[0];
            T instance = null;
            IWrapperGenerator wg = IOC.resolve(Keys.resolveByName(IWrapperGenerator.class.getCanonicalName()));
            Object unknown = IOC.resolve(Keys.resolveByName((String) args[1]));
            if (unknown.getClass().isInterface()) {
                instance = wg.generate((Class<T>) unknown);
            }
            ((IObjectWrapper) instance).init(obj);

            return instance;

        } catch (Throwable e) {
            throw new ResolutionStrategyException(
                    "Could not convert given instance of IObject to the specific wrapper.",
                    e
            );
        }
    }
}
