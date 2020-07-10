package info.smart_tools.smartactors.core.scope_creation_event_handler;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.istrategy_container.IStrategyContainer;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.scope.iscope.IScope;

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
     * @throws InvalidArgumentException if any errors occurred
     */
    public ScopeCreationEventHandler(final IKey id)
            throws InvalidArgumentException {
        if (null == id) {
            throw new InvalidArgumentException("Key should not be null.");
        }
        this.id = id;
    }

    /**
     * Add instance of {@link IStrategyContainer}
     * @param createdScope instance of {@link IScope}
     * @throws ActionExecutionException if any errors occurred
     * @throws InvalidArgumentException if incoming argument are null
     */
    @Override
    public void execute(final IScope createdScope)
            throws ActionExecutionException, InvalidArgumentException {
        if (null == createdScope) {
            throw new InvalidArgumentException("Argument should not be null.");
        }
        try {
            IStrategyContainer container = new StrategyContainer();
            createdScope.setValue(this.id, container);
        } catch (Exception e) {
            throw new ActionExecutionException("Execution error.", e);
        }
    }
}
