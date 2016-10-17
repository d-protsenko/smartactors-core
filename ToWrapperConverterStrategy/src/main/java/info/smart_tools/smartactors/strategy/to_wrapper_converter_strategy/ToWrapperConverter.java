package info.smart_tools.smartactors.strategy.to_wrapper_converter_strategy;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * Implementation of {@link IResolveDependencyStrategy}.
 * Convert given instance of {@link IObject}
 * to the specific wrapper
 */
public class ToWrapperConverter implements IResolveDependencyStrategy {

    @Override
    public <T> T resolve(final Object ... args)
            throws ResolveDependencyStrategyException {

        try {
            IObject obj = (IObject) args[0];
            T instance = null;
            IWrapperGenerator wg = IOC.resolve(Keys.getOrAdd(IWrapperGenerator.class.getCanonicalName()));
            Object unknown = IOC.resolve(Keys.getOrAdd((String) args[1]));
            if (unknown.getClass().isInterface()) {
                instance = wg.generate((Class<T>) unknown);
            }
            ((IObjectWrapper) instance).init(obj);

            return instance;

        } catch (Throwable e) {
            throw new ResolveDependencyStrategyException(
                    "Could not convert given instance of IObject to the specific wrapper.",
                    e
            );
        }
    }
}
