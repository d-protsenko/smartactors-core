package info.smart_tools.smartactors.base.interfaces.iresolution_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

/**
 * IResolutionStrategy
 */
public interface IResolutionStrategy {

    /**
     * Resolve dependency by realized strategy
     * @param args array of needed parameters for resolve dependency
     * @param <T> type of object
     * @return instance of type T object
     * @throws ResolutionStrategyException if any errors occurred
     */
    <T> T resolve(final Object ... args)
            throws ResolutionStrategyException;
}
