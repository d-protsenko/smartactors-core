package info.smart_tools.smartactors.actor.change_password;

import info.smart_tools.smartactors.actor.change_password.exception.ChangePasswordException;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordMessage;
import info.smart_tools.smartactors.actor.change_password.wrapper.User;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.insert.psql.DBInsertTask;
import info.smart_tools.smartactors.core.db_task.search.psql.PSQLSearchTask;
import info.smart_tools.smartactors.core.db_task.search.utils.IBufferedQuery;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.wrapper_generator.IObjectWrapper;

/**
 * Changes user password
 */
public class ChangePasswordActor {

    private CollectionName collectionName;
    private IBufferedQuery bufferedQuery;

    private static final Field<IObject> USER_ID_F;
    private static final Field<String> EQUALS_F;
    private static final IFieldName MESSAGE_FN;

    static {
        try {
            USER_ID_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "userId"));
            EQUALS_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "$eq"));
            MESSAGE_FN = new FieldName("message");
        } catch (ResolutionException | InvalidArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Constructor
     * @param params contains collection name
     */
    public ChangePasswordActor(final ChangePasswordConfig params) {
        try {
            this.collectionName = params.getCollectionName();
        } catch (ReadValueException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes user password
     * @param message {
     *                "userId": "identifier for search user",
     *                "password": "new user's password"
     * }
     * @throws ChangePasswordException for any occurred error
     */
    public void changePassword(final ChangePasswordMessage message) throws ChangePasswordException {

        //TODO:: clarify about PG resolving
        try (IPoolGuard poolGuard = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"))) {
            IDatabaseTask searchTask = IOC.resolve(Keys.getOrAdd(PSQLSearchTask.class.toString()));
            ISearchQuery searchQuery = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()));

            StorageConnection connection = IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject());
            searchTask.setConnection(connection);
            searchTask.prepare(prepareQueryMsg(searchQuery, message));
            searchTask.execute();

            this.bufferedQuery = searchQuery
                .getBufferedQuery()
                .orElseThrow(() -> new ChangePasswordException("Search task didn't returned a buffered query!"));

            if (searchQuery.countSearchResult() == 0) {
                throw new ChangePasswordException("Error during change password: can't find user by identifier: " + message.getUserId());
            }

            IObject userObj = searchQuery.getSearchResult(0);
            User user = IOC.resolve(Keys.getOrAdd(User.class.toString()));
            ((IObjectWrapper) user).init(userObj);
            //TODO:: password validation and encoding
            user.setPassword(message.getPassword());

            IDatabaseTask insertTask = IOC.resolve(Keys.getOrAdd(DBInsertTask.class.toString()));
            //TODO:: uncomment when IUpsertQueryMessage would be added to dev
//            IUpsertQueryMessage queryMessage = IOC.resolve(Keys.getOrAdd(IUpsertQueryMessage.class.toString()));
            insertTask.setConnection(connection);
//            insertTask.prepare(prepareInsertQuery(queryMessage, userObj));
            insertTask.execute();


        } catch (ReadValueException | TaskSetConnectionException | TaskExecutionException | ResolutionException | ChangeValueException
            | InvalidArgumentException | TaskPrepareException e) {
            throw new ChangePasswordException("Error during change password.", e);
        }
    }

    private IObject prepareQueryMsg(final ISearchQuery searchQuery, final ChangePasswordMessage message)
        throws ResolutionException, ChangeValueException, ReadValueException, InvalidArgumentException {

        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
        IObject userIdObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

        EQUALS_F.in(userIdObject, message.getUserId());
        USER_ID_F.in(query, userIdObject);

        searchQuery.setCollectionName(collectionName.toString());
        searchQuery.setPageSize(1);
        searchQuery.setPageNumber(1);
        searchQuery.setBufferedQuery(bufferedQuery);
        searchQuery.setCriteria(query);

        IObject[] initObjects = IOC.resolve(
            Keys.getOrAdd(IObjectWrapper.class.toString() + ".getIObjects"),
            searchQuery);

        return initObjects[0];
    }

    //TODO:: uncomment when IUpsertQueryMessage would be added to dev
//    private IObject prepareInsertQuery(final IUpsertQueryMessage insertQuery, final IObject user) throws ResolutionException {
//
//        insertQuery.setCollectionName(collectionName);
//        insertQuery.setDocument(user);
//        return ((IObjectWrapper) insertQuery).getEnvironmentIObject(MESSAGE_FN);
//    }
}
