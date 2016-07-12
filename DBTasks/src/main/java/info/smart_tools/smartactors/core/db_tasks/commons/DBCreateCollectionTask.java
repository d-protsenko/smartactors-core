package info.smart_tools.smartactors.core.db_tasks.commons;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.core.db_storage.interfaces.ICompiledQuery;
import info.smart_tools.smartactors.core.db_tasks.wrappers.create_collection.ICreateCollectionMessage;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;

import javax.annotation.Nonnull;

public abstract class DBCreateCollectionTask extends GeneralDatabaseTask {

    protected DBCreateCollectionTask() {}

    @Override
    protected boolean requiresExecutable(@Nonnull IObject queryMessage) throws InvalidArgumentException {
        return true;
    }

    @Override
    protected void execute(@Nonnull final ICompiledQuery compiledQuery,
                           @Nonnull final IObject queryMessage
    ) throws TaskExecutionException {
        try {
            compiledQuery.execute();
        } catch (QueryExecutionException e) {
            throw new TaskExecutionException("'Create collection task' execution has been failed because: ", e);
        }
    }
}
