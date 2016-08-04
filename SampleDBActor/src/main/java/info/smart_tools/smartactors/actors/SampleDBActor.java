package info.smart_tools.smartactors.actors;

import info.smart_tools.smartactors.actors.exception.SampleDBException;
import info.smart_tools.smartactors.actors.wrapper.SampleDBWrapper;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;

/**
 * Sample actor which upserts and retreives the document from database.
 */
public class SampleDBActor {

    public void getDocumentById(SampleDBWrapper wrapper)
            throws SampleDBException {
        try {
            ConnectionOptions options = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            IPool pool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), options);

            String collectionName = wrapper.getCollectionName();
            IObject document = wrapper.getDocument();

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        guard.getObject(),
                        collectionName,
                        document
                );
                task.execute();
            }

            IFieldName idField = IOC.resolve(
                    Keys.getOrAdd(IFieldName.class.getCanonicalName()), collectionName + "ID");
            Object id = document.getValue(idField);

            try (PoolGuard guard = new PoolGuard(pool)) {
                ITask task = IOC.resolve(
                        Keys.getOrAdd("db.collection.getbyid"),
                        guard.getObject(),
                        wrapper.getCollectionName(),
                        id,
                        (IAction<IObject>) doc -> {
                            try {
                                wrapper.setDocument(doc);
                            } catch (ChangeValueException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                task.execute();
            }
        } catch (Exception e) {
            throw new SampleDBException(e);
        }
    }
}
