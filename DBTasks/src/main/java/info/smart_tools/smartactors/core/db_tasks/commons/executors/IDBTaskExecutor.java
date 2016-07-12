package info.smart_tools.smartactors.core.db_tasks.commons.executors;

import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

/**
 *
 */
public interface IDBTaskExecutor {
    /**
     *
     * @param message
     * @return
     * @throws InvalidArgumentException
     */
    public abstract boolean requiresExecutable(@Nonnull final IObject message) throws InvalidArgumentException;

    /**
     *
     * @param query
     * @param message
     */
    abstract void execute(@Nonnull final ICompiledQuery query, @Nonnull final IObject message) throws TaskExecutionException;
}
