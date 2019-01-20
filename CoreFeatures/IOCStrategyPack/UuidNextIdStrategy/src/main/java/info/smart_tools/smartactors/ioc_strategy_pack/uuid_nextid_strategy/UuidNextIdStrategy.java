package info.smart_tools.smartactors.ioc_strategy_pack.uuid_nextid_strategy;

import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.IResolutionStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolution_strategy.exception.ResolutionStrategyException;

import java.util.UUID;

/**
 * The strategy which generates new UUID to be used as ID for the new document in the collection
 * or anywhere else.
 * {@link java.util.UUID}
 */
public class UuidNextIdStrategy implements IResolutionStrategy {
    @Override
    public String resolve(final Object... args) throws ResolutionStrategyException {
        return UUID.randomUUID().toString();
    }
}
