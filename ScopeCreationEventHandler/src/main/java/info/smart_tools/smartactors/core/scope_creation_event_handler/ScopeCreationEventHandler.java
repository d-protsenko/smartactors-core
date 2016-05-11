package info.smart_tools.smartactors.core.scope_creation_event_handler;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobserver.IObserver;
import info.smart_tools.smartactors.core.iobserver.exception.ObserverExecuteException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;

/**
 * Implementation of {@link IObserver}
 *
 */
public class ScopeCreationEventHandler implements IObserver<IScope> {

    /**
     * ID of current handler
     */
    private IKey id;

    /**
     * Constructor by unique handler identifier
     * @param id unique handler identifier
     */
    public ScopeCreationEventHandler(final IKey id) {
        this.id = id;
    }

    /**
     * Add instance of {@link IStrategyContainer}
     * @param createdScope instance of {@link IScope}
     * @throws ObserverExecuteException if any errors occurred
     */
    @Override
    public void execute(final IScope createdScope)
            throws ObserverExecuteException {
        try {
            IStrategyContainer container = new StrategyContainer();
            createdScope.setValue(this.id, container);

        } catch (Exception e) {
            throw new ObserverExecuteException("Execution error.", e);
        }
    }
}
