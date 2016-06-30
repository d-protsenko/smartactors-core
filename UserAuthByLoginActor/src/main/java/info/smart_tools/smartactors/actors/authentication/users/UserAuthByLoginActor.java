package info.smart_tools.smartactors.actors.authentication.users;

import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMsg;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.psql.PSQLSearchTask;
import info.smart_tools.smartactors.core.db_task.search.utils.IBufferedQuery;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.sql_commons.DBTaskExecutor;
import info.smart_tools.smartactors.core.wrapper_generator.Field;

/**
 *
 */
public class UserAuthByLoginActor {
    private String collection;
    private IBufferedQuery bufferedQuery;

    private static final Field<IObject> LOGIN_F;
    private static final Field<IObject> PASSWORD_F;
    private static final Field<String> EQUALS_F;

    static {
        try {
            LOGIN_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "email"));
            PASSWORD_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "password"));
            EQUALS_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "$eq"));
        } catch (ResolutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param params
     * @throws InvalidArgumentException
     */
    public UserAuthByLoginActor(final IUserAuthByLoginParams params) throws InvalidArgumentException {
        checkParams(params);
        collection = params.getCollection();
    }

    /**
     *
     * @param message
     * @throws InvalidArgumentException
     * @throws AuthenticateUserException
     */
    public void authenticateUser(final IUserAuthByLoginMsg message)
            throws InvalidArgumentException, AuthenticateUserException {
        checkMsg(message);
        try (IPoolGuard poolGuard = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"))) {
            StorageConnection connection = IOC.resolve(
                    Keys.getOrAdd(StorageConnection.class.toString()),
                    poolGuard.getObject());

            IDatabaseTask searchTask = IOC.resolve(Keys.getOrAdd(PSQLSearchTask.class.toString()));
            IObject preparedSearchMsg = prepareQueryMsg(message);
            searchTask.setConnection(connection);
            searchTask.prepare(preparedSearchMsg);
            searchTask.execute();

        } catch (ResolutionException | TaskSetConnectionException |
                TaskPrepareException | TaskExecutionException | ChangeValueException e) {
            throw new AuthenticateUserException();
        }
    }

    private IObject prepareQueryMsg(final IUserAuthByLoginMsg message)
            throws ResolutionException, ChangeValueException, InvalidArgumentException {

        ISearchQuery searchQuery = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()));
        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
        IObject loginObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
        IObject passwordObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

        EQUALS_F.inject(loginObject, message.getLogin());
        EQUALS_F.inject(passwordObject, message.getPassword());
        LOGIN_F.inject(query, loginObject);
        PASSWORD_F.inject(query, passwordObject);

        searchQuery.setCollectionName(collection);
        searchQuery.setPageSize(1);
        searchQuery.setPageNumber(1);
        searchQuery.setBufferedQuery(bufferedQuery);
        searchQuery.setCriteria(query);

        return IOC.resolve(Keys.getOrAdd(IObject.class.toString()), searchQuery);
    }

    private void checkParams(final IUserAuthByLoginParams params) throws InvalidArgumentException {
        if (isNullOrEmpty(params.getCollection())) {
            throw new InvalidArgumentException();
        }
    }

    private void checkMsg(final IUserAuthByLoginMsg msg) throws InvalidArgumentException {
        if (isNullOrEmpty(msg.getLogin()) || isNullOrEmpty(msg.getPassword())) {
            throw new InvalidArgumentException();
        }
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
