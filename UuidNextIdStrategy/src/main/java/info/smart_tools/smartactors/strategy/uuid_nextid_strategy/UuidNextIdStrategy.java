package info.smart_tools.smartactors.strategy.uuid_nextid_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.UUID;

/**
 * The strategy which generates new UUID to be used as ID for the new document in the collection
 * or anywhere else.
 * {@link java.util.UUID}
 */
public class UuidNextIdStrategy implements IResolveDependencyStrategy {
    @Override
    public String resolve(final Object... args) throws ResolveDependencyStrategyException {
        return UUID.randomUUID().toString();
    }
}
