package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;

import java.util.UUID;

/**
 * The strategy which generates new UUID to be used as ID for the new document in the collection.
 * {@link java.util.UUID}
 */
public class UuidNextIdStrategy implements IResolveDependencyStrategy {
    @Override
    public Object resolve(Object... args) throws ResolveDependencyStrategyException {
        return UUID.randomUUID().toString();
    }
}
