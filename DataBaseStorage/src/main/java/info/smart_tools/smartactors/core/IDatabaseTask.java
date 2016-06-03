package info.smart_tools.smartactors.core;

import info.smart_tools.smartactors.core.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.iobject.IObject;

/**
 * Database oriented task
 * @see ITask
 * TODO:: make this interface extends ITask and remove execute(), when ITask would be merged into develop
 */
public interface IDatabaseTask {

    /**
     * Prepares database query
     * @param query
     * @throws TaskPrepareException if error occurs in process of query preparing
     */
    void prepare(IObject query) throws TaskPrepareException;
    /**
     * Execute the task.
     *
     * @throws TaskExecutionException if error occurs in process of task execution
     */
    void execute() throws TaskExecutionException;
}
