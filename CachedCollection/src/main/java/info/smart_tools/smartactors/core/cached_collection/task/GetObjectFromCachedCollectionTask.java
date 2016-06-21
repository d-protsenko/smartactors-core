package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectsFromCachedCollectionParameters;
import info.smart_tools.smartactors.core.cached_collection.wrapper.SearchCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;
import java.util.Arrays;

public class GetObjectFromCachedCollectionTask implements IDatabaseTask {
    private IDatabaseTask targetTask;
    private String collectionName;

    private IFieldName eqFieldName;
    private IFieldName dateToFieldName;
    private IFieldName keyFieldName;

    public GetObjectFromCachedCollectionTask(final GetObjectsFromCachedCollectionParameters params) {
        try {
            this.targetTask = params.getTask();
            this.collectionName = params.getCollectionName();

            eqFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "$eq");
            dateToFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "$date-to");
            keyFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), params.getKey());
        } catch (ReadValueException | ChangeValueException | ResolutionException e) {
            // TODO: Throwing standardInitialisationException
        }
    }

    @Override
    public void prepare(IObject query) throws TaskPrepareException {
        try {
            IObject criteriaIObject = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));

            SearchCachedCollectionQuery criteriaQuery = IOC.resolve(Keys.getOrAdd(SearchCachedCollectionQuery.class.toString()), criteriaIObject);

            try {
                String key = query.getValue(keyFieldName).toString();
                IObject keyCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
                keyCriteria.setValue(eqFieldName, key);
                criteriaIObject.setValue(keyFieldName, keyCriteria);
            } catch (ReadValueException e) {
                throw new TaskPrepareException("Can't get required parameter: " + keyFieldName.toString(), e);
            }

            IObject isActiveCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            isActiveCriteria.setValue(eqFieldName, true);
            criteriaQuery.setIsActive(isActiveCriteria);

            IObject startDateTimeCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            startDateTimeCriteria.setValue(dateToFieldName, LocalDateTime.now());
            criteriaQuery.setStartDateTime(startDateTimeCriteria);

            ISearchQuery iSearchQuery = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()), query);

            iSearchQuery.setCollectionName(collectionName);
            iSearchQuery.setPageNumber(0);
            iSearchQuery.setPageSize(100);// FIXME: 6/21/16 hardcode count must be fixed
            iSearchQuery.setCriteria(criteriaIObject);

            targetTask.prepare(query);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create ISearchQuery from input query", e);
        } catch (ChangeValueException e) {
            throw new TaskPrepareException("Can't change value in one of IObjects", e);
        }
    }

    @Override
    public void setConnection(StorageConnection connection) throws TaskSetConnectionException {
        targetTask.setConnection(connection);
    }

    @Override
    public void execute() throws TaskExecutionException {
        targetTask.execute();
    }
}
