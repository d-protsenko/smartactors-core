package info.smart_tools.smartactors.core.scheduler.interfaces;

import info.smart_tools.smartactors.core.scheduler.interfaces.exceptions.SchedulingStrategyExecutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;

/**
 * Interface for a strategy defining when to execute tasks associated with scheduler entries.
 */
public interface ISchedulingStrategy {
    /**
     * Called by scheduler just after a entry was created with this strategy.
     *
     * @param entry    the entry
     * @param args     the arguments the entry was created with
     * @throws SchedulingStrategyExecutionException if any error occurs
     */
    void init(final ISchedulerEntry entry, final IObject args) throws SchedulingStrategyExecutionException;

    /**
     * Called by scheduler after the message associated with the entry was sent.
     *
     * @param entry    the entry
     * @throws SchedulingStrategyExecutionException if any error occurs
     */
    void postProcess(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException;

    /**
     * Called by scheduler when the entry is downloaded from database.
     *
     * @param entry    the entry
     * @throws SchedulingStrategyExecutionException if any error occurs
     */
    void restore(final ISchedulerEntry entry) throws SchedulingStrategyExecutionException;

    /**
     * Called by scheduler when an exception occurs sending the message associated with the given entry or executing {@link
     * #postProcess(ISchedulerEntry)} method of this strategy.
     *
     * @param entry    the entry
     * @param e        the exception
     * @throws SchedulingStrategyExecutionException if any error occurs
     */
    void processException(final ISchedulerEntry entry, final Throwable e) throws SchedulingStrategyExecutionException;
}
