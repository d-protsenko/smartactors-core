package info.smart_tools.smartactors.core.ioc;

import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;

/**
 * Interface of IOC
 */
public interface IContainer {

    /**
     * @param classId unique class identifier
     * @param arg needed arguments for resolve dependency
     * @throws ResolutionException when resolution is impossible because of any error
     * @return instance of class with classId identifier
     */
    Object resolve(final Object classId, final Object... arg)
            throws ResolutionException;

    /**
     * Connect specific class with classId to specific strategy.
     * After registration this class may be resolved with selected strategy
     * @param classId unique class identifier
     * @param strategyId unique strategy identifier
     * @throws RegistrationException when registration is impossible because of any error
     */
    void register(final Object classId, final Object strategyId)
            throws RegistrationException;
}