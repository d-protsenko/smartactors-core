package info.smart_tools.smartactors.core.cached_collection.task;

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
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;
import java.util.Arrays;

public class GetObjectFromCachedCollection implements IDatabaseTask {
    private IDatabaseTask targetTask;
    private String key;
    private String collectionName;

    private IFieldName startDateTimeFieldName;

    private IFieldName eqFieldName;
    private IFieldName ltFieldName;

    public GetObjectFromCachedCollection(IDatabaseTask targetTask, String key, String collectionName) throws ResolutionException {
        this.targetTask = targetTask;
        this.key = key;
        this.collectionName = collectionName;
        eqFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "$eq");
        ltFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "$lt");
        startDateTimeFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), "startDateTime");
    }

    @Override
    public void prepare(IObject query) throws TaskPrepareException {
        try {
            IObject criteriaIObject = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));

            SearchCachedCollectionQuery criteriaQuery = IOC.resolve(Keys.getOrAdd(SearchCachedCollectionQuery.class.toString()), criteriaIObject);

            IObject keyCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            keyCriteria.setValue(eqFieldName, key);
            criteriaQuery.setKey(keyCriteria);

            IObject isActiveCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            isActiveCriteria.setValue(eqFieldName, true);
            criteriaQuery.setIsActive(isActiveCriteria);

            IObject startDateTimeCriteria = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            startDateTimeCriteria.setValue(ltFieldName, LocalDateTime.now());
            criteriaQuery.setStartDateTime(startDateTimeCriteria);

            IObject startDateTimeOrder = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
            startDateTimeOrder.setValue(startDateTimeFieldName, "DESC");

            ISearchQuery iSearchQuery = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()), query);

            iSearchQuery.setCollectionName(collectionName);
            iSearchQuery.setPageNumber(0);
            iSearchQuery.setPageSize(1);
            iSearchQuery.setCriteria(criteriaIObject);
            iSearchQuery.setOrderBy(Arrays.asList(startDateTimeOrder));

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
