package info.smart_tools.smartactors.shutdown.ishutdown_aware_task;

/**
 * Interface for managed task representation that allows to notify a task about pending shutdown request.
 */
public interface IShutdownAwareTask {
    /**
     * Notify this task that there is a shutdown request.
     */
    void notifyShuttingDown();
}
