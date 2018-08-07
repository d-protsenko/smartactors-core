package info.smart_tools.smartactors.ioc.iioccontainer;


import info.smart_tools.smartactors.ioc.iioccontainer.exception.DeletionException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;

/**
 * Interface of IOC
 */
public interface IContainer {

    /**
     * Return specific instance of {@link IKey} for container ID
     * @return instance of {@link IKey}
     */
    IKey getIocKey();

    /**
     * Return specific instance of {@link IKey} for resolve dependencies from key storage
     * @return instance of {@link IKey}
     */
    @Deprecated
    IKey getKeyForKeyStorage();

    /**
     * Return specific instance of {@link IKey} for strategy of resolving key by name
     * @return instance of {@link IKey}
     */
    IKey getKeyForKeyByNameResolveStrategy();

    /**
     * Resolve dependency by given given {@link IKey} instance and args
     * @param key instance of {@link IKey}
     * @param args needed parameters for resolve dependency
     * @param <T> type of class for resolution
     * @return instance of class with classId identifier
     * @throws ResolutionException if resolution is impossible because of any errors
     */
    <T> T resolve(final IKey key, final Object... args)
            throws ResolutionException;

    /**
     * Register new dependency by instance of {@link IKey}
     * @param key instance of {@link IKey}
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws RegistrationException when registration is impossible because of any error
     */
    void register(final IKey key, final IResolveDependencyStrategy strategy)
            throws RegistrationException;

    /**
     * Remove dependency with given key
     * @param key instance of {@link IKey}
     * @throws DeletionException if any errors occurred
     */
    void remove(final IKey key)
            throws DeletionException;
}