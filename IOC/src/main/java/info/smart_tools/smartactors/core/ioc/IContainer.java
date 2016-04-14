package info.smart_tools.smartactors.core.ioc;

import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;

/**
 * Interface of IOC
 */
public interface IContainer {

    /**
     * Resolve dependency for specific class by specific arguments
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @throws ResolutionException when resolution is impossible because of any error
     * @return instance of class with specific identifier
     */
    Object resolve(final IObject obj)
            throws ResolutionException;

    /**
     * Register new dependency
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @throws RegistrationException when registration is impossible because of any error
     */
    void register(final IObject obj)
            throws RegistrationException;
}