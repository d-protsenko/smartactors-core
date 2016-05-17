package info.smart_tools.smartactors.core.scope_creation_event_handler;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.core.strategy_container.StrategyContainer;

/**
 * Implementation of {@link IAction}
 *
 */
public class ScopeCreationEventHandler implements IAction<IScope> {

    /**
     * ID of current handler
     */
    private IKey id;

    /**
     * Constructor by unique handler identifier
     * @param id unique handler identifier
     */
    public ScopeCreationEventHandler(final IKey id) {
        if (id == null) {
            throw new IllegalArgumentException("Key should not be null.");
        }
        this.id = id;
    }

    /**
     * Add instance of {@link IStrategyContainer}
     * @param createdScope instance of {@link IScope}
     * @throws ActionExecuteException if any errors occurred
     */
    @Override
    public void execute(final IScope createdScope)
            throws ActionExecuteException {
        if (createdScope == null) {
            throw new IllegalArgumentException("Argument should not be null.");
        }
        try {
            IStrategyContainer container = new StrategyContainer();
            createdScope.setValue(this.id, container);
        } catch (Exception e) {
            throw new ActionExecuteException("Execution error.", e);
        }
    }
}
