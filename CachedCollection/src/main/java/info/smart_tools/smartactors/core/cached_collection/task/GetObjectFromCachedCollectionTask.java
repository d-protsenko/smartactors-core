package info.smart_tools.smartactors.core.cached_collection.task;

import info.smart_tools.smartactors.core.cached_collection.wrapper.DBSearchWrappers.DateToMessage;
import info.smart_tools.smartactors.core.cached_collection.wrapper.DBSearchWrappers.EQMessage;
import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectFromCachedCollectionQuery;
import info.smart_tools.smartactors.core.cached_collection.wrapper.GetObjectsFromCachedCollectionParameters;
import info.smart_tools.smartactors.core.cached_collection.wrapper.CriteriaCachedCollectionQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;

public class GetObjectFromCachedCollectionTask implements IDatabaseTask {
    private IDatabaseTask targetTask;

    public GetObjectFromCachedCollectionTask(final GetObjectsFromCachedCollectionParameters params) throws InvalidArgumentException {
        try {
            this.targetTask = params.getTask();
        } catch (ReadValueException | ChangeValueException e) {
            throw new InvalidArgumentException("Can't create GetObjectFromCachedCollectionTask.", e);
        }
    }

    @Override
    public void prepare(IObject query) throws TaskPrepareException {
        try {
            GetObjectFromCachedCollectionQuery srcQueryObject = IOC.resolve(Keys.getOrAdd(GetObjectFromCachedCollectionQuery.class.toString()), query);

            CriteriaCachedCollectionQuery criteriaQuery = IOC.resolve(Keys.getOrAdd(CriteriaCachedCollectionQuery.class.toString()), getResolvedIObject());

            EQMessage keyEQ = IOC.resolve(Keys.getOrAdd(EQMessage.class.toString()), getResolvedIObject());
            keyEQ.setEq(srcQueryObject.getKey());
            criteriaQuery.setKey(keyEQ);

            EQMessage isActiveEQ = IOC.resolve(Keys.getOrAdd(EQMessage.class.toString()), getResolvedIObject());
            isActiveEQ.setEq(Boolean.toString(true));
            criteriaQuery.setIsActive(isActiveEQ);

            DateToMessage startDateTimeDateTo = IOC.resolve(Keys.getOrAdd(DateToMessage.class.toString()), getResolvedIObject());
            startDateTimeDateTo.setDateTo(LocalDateTime.now().toString());
            criteriaQuery.setStartDateTime(startDateTimeDateTo);

            srcQueryObject.setPageNumber(0);
            srcQueryObject.setPageSize(100);// FIXME: 6/21/16 hardcode count must be fixed
            srcQueryObject.setCriteria(criteriaQuery);

            targetTask.prepare(query);
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create ISearchQuery from input query", e);
        } catch (ChangeValueException | ReadValueException e) {
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

    private static IObject getResolvedIObject() throws ResolutionException {
        return IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IObject.class.toString()));
    }
}
