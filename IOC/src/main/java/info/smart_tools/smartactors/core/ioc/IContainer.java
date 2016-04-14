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
     * @param obj obj instance of IObject that contains needed parameters for resolve dependency
     * @param <T> type of resolved object
     * @return instance of class with specific identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    <T> T resolve(final IObject obj)
            throws ResolutionException;

    /**
     * Register new dependency
     * @param obj instance of IObject that contains needed parameters for resolve dependency
     * @throws RegistrationException when registration is impossible because of any errors
     */
    void register(final IObject obj)
            throws RegistrationException;
}