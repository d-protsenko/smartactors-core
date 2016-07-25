package info.smart_tools.smartactors.core.postgres_upsert_task;

import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

/**
 * The database task which is able to upsert documents into Postgres database.
 */
public class PostgresUpsertTask implements IDatabaseTask {

    /**
     * Pattern for the document field with the document ID.
     */
    private static final String ID_FIELD_PATTERN = "%sID";

    /**
     * Collection where the document should be upserted.
     */
    private CollectionName collection;

    /**
     * Document to be upserted.
     */
    private IObject document;

    /**
     * Name of the ID field in the document.
     */
    private IFieldName idField;

    @Override
    public void execute() throws TaskExecutionException {
        try {
            document.setValue(idField, "STUB");     // TODO
        } catch (InvalidArgumentException | ChangeValueException e) {
            throw new TaskExecutionException(e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            UpsertMessage message = IOC.resolve(Keys.getOrAdd(UpsertMessage.class.getCanonicalName()), query);
            collection = message.getCollectionName();
            idField = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), String.format(ID_FIELD_PATTERN, collection.toString()));
            document = message.getDocument();
        } catch (ResolutionException e) {
            throw new TaskPrepareException(e);
        }
    }

    @Override
    public void setConnection(StorageConnection connection) throws TaskSetConnectionException {

    }
}
