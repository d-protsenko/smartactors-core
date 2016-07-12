package info.smart_tools.smartactors.core.db_tasks.commons.executors;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

public class DBCreateCollectionTaskExecutor implements IDBTaskExecutor {

    private DBCreateCollectionTaskExecutor() { }

    public static DBCreateCollectionTaskExecutor create() {
        return new DBCreateCollectionTaskExecutor();
    }

    @Override
    public boolean requiresExecutable(@Nonnull final IObject queryMessage) throws InvalidArgumentException {
        return true;
    }

    @Override
    public void execute(@Nonnull final ICompiledQuery compiledQuery,
                        @Nonnull final IObject queryMessage
    ) throws TaskExecutionException {
        try {
            compiledQuery.execute();
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Create collection task' execution has been failed because: ", e);
        }
    }
}
