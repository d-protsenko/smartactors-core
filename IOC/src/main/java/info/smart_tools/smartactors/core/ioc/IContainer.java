package info.smart_tools.smartactors.core.ioc;

import info.smart_tools.smartactors.core.ioc.exception.RegistrationException;
import info.smart_tools.smartactors.core.ioc.exception.ResolutionException;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Interface of IOC
 */
public interface IContainer {

    /**
     * Resolve dependency by given given {@link IKey} instance and args
     * @param key instance of {@link IKey}
     * @param args needed parameters for resolve dependency
     * @param <T> type of class for resolution
     * @return instance of class with classId identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    <T> T resolve(final IKey<T> key, final Object ... args)
            throws ResolutionException;

    /**
     * Register new dependency by instance of {@link IKey}
     * @param key instance of {@link IKey}
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws RegistrationException when registration is impossible because of any error
     */
    void register(final IKey key, final IResolveDependencyStrategy strategy)
            throws RegistrationException;
}