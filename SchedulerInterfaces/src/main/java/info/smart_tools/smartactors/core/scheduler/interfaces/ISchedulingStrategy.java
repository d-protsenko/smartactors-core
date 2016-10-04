package info.smart_tools.smartactors.core.scheduler.interfaces;

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
     */
    void init(final ISchedulerEntry entry, final IObject args);

    /**
     * Called by scheduler after the message associated with the entry was sent.
     *
     * @param entry    the entry
     */
    void postProcess(final ISchedulerEntry entry);

    /**
     * Called by scheduler when the entry is downloaded from database.
     *
     * @param entry    the entry
     */
    void restore(final ISchedulerEntry entry);

    /**
     * Called by scheduler when an exception occurs sending the message associated with the given entry or executing {@link
     * #postProcess(ISchedulerEntry)} method of this strategy.
     *
     * @param entry    the entry
     * @param e        the exception
     */
    void processException(final ISchedulerEntry entry, final Throwable e);
}
