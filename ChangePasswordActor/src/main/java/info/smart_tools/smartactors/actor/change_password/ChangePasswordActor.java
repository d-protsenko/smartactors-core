package info.smart_tools.smartactors.actor.change_password;

import info.smart_tools.smartactors.actor.change_password.exception.ChangePasswordException;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordMessage;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.security.encoding.encoders.EncodingException;
import info.smart_tools.smartactors.security.encoding.encoders.IPasswordEncoder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Changes user password
 */
public class ChangePasswordActor {

    private static final String CHANGE_PASSWORD_ERROR_MSG = "Changing password has been failed because: ";

    private String collectionName;
    private IPool connectionPool;
    private IPasswordEncoder passwordEncoder;

    private IField userIdF;
    private IField passwordF;
    private IField equalsF;

    private IField collectionNameF;
    private IField pageSizeF;
    private IField pageNumberF;
    private IField pageF;
    private IField filterF;

    /**
     * Constructor
     * @param params contains collection name
     * @throws ChangePasswordException if error during create is occurred
     */
    public ChangePasswordActor(final ChangePasswordConfig params) throws ChangePasswordException {
        try {
            userIdF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "userId");
            passwordF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "password");
            equalsF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "$eq");

            collectionNameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            pageSizeF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "size");
            pageNumberF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "number");
            pageF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "page");
            filterF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "filter");

            this.collectionName = params.getCollectionName();
            this.connectionPool = params.getConnectionPool();
            this.passwordEncoder = IOC.resolve(
                Keys.getOrAdd("PasswordEncoder"),
                params.getAlgorithm(),
                params.getEncoder(),
                params.getCharset()
            );
        } catch (ResolutionException | ReadValueException e) {
            throw new ChangePasswordException("Can't create change password actor");
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

        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            if (isNullOrEmpty(message.getUserId()) || isNullOrEmpty(message.getPassword())) {
                setFailResponse(message, "User identifier or password is empty");
                throw new ChangePasswordException("Invalid message format!");
            }

            IObject searchQuery = prepareQueryParams(message);
            List<IObject> items = new LinkedList<>();
            ITask searchTask = IOC.resolve(
                Keys.getOrAdd("db.collection.search"),
                poolGuard.getObject(),
                collectionName,
                searchQuery,
                (IAction<IObject[]>) foundDocs -> {
                    try {
                        items.addAll(Arrays.asList(foundDocs));
                    } catch (Exception e) {
                        throw new ActionExecuteException(e);
                    }
                }
            );
            searchTask.execute();

            validateSearchResult(items, message);
            IObject user = items.get(0);
            passwordF.out(user, passwordEncoder.encode(message.getPassword()));

            ITask upsertTask = IOC.resolve(
                Keys.getOrAdd("db.collection.upsert"),
                poolGuard.getObject(),
                collectionName,
                user
            );
            upsertTask.execute();
        } catch (PoolGuardException | ReadValueException | TaskExecutionException | ResolutionException |
            ChangeValueException | InvalidArgumentException | EncodingException e) {

            throw new ChangePasswordException("Error during change password.", e);
        }
    }

    private void validateSearchResult(final List<IObject> searchResult, final ChangePasswordMessage message)
        throws ChangePasswordException {

        try {
            if (searchResult.isEmpty()) {
                setFailResponse(message, "Can't find user with such identifier: " + message.getUserId());
                throw new ChangePasswordException(CHANGE_PASSWORD_ERROR_MSG +
                    "user with identifier: [" + message.getUserId() + "] doesn't exist!");
            }
            if (searchResult.size() > 1) {
                setFailResponse(message, "There are several users with such identifier: " + message.getUserId());
                throw new ChangePasswordException(CHANGE_PASSWORD_ERROR_MSG +
                    "too many users with identifier: [" + message.getUserId() + "]!");
            }
        } catch (ReadValueException | ChangeValueException e) {
            throw new ChangePasswordException(CHANGE_PASSWORD_ERROR_MSG + e.getMessage(), e);
        }
    }

    private IObject prepareQueryParams(final ChangePasswordMessage message)
        throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException {

        IObject filter = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject page = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject searchQuery = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        collectionNameF.out(searchQuery, this.collectionName);
        pageSizeF.out(page, 1);
        pageNumberF.out(page, 1);
        pageF.out(searchQuery, page);
        filterF.out(searchQuery, filter);

        IObject userIdObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        equalsF.out(userIdObject, message.getUserId());
        userIdF.out(filter, userIdObject);

        return searchQuery;
    }

    private void setFailResponse(final ChangePasswordMessage message, final String errorMessage) throws ChangeValueException {
        message.setAuthStatus("FAIL");
        message.setAuthMessage(errorMessage);
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
