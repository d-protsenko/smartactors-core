package info.smart_tools.smartactors.actors.authentication.users;

import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMessage;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.security.encoding.encoders.EncodingException;
import info.smart_tools.smartactors.core.security.encoding.encoders.IPasswordEncoder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Actor for authenticate users by given login and password.
 *
 */
public class UserAuthByLoginActor {
    /** Name of a some collection in database where is users documents. */
    private String collection;
    private IPool connectionPool;

    /** Encoder for obtaining a some hash of given user's password. */
    private IPasswordEncoder passwordEncoder;

    private static final IField LOGIN_F;
    private static final IField PASSWORD_F;
    private static final IField EQUALS_F;

    /* ToDo : Needs message source. */
    private static final String AUTH_ERROR_MSG = "User authentication has been failed because: ";

    private static final String AUTH_ERROR_RESPONSE_MSG = "Такой почтовый адрес не зарегистрирован, либо пароль неверный. " +
            "Если Вы уверены, что регистрировались на нашем сайте, но не помните пароль, " +
            "то попробуйте его восстановить ниже по форме. Просто введите свой электронный адрес, " +
            "и Вам на электронную почту придет ссылка. Пройдя по ссылке, " +
            "Вы сможете ввести новый удобный для Вас пароль.";

    private static final String INTERNAL_ERROR_MSG = "Во время обработки запроса произошла ошибка. " +
            "Пожалуйста попробуйте повторить операцию. Приносим свои извинения за доставленные неудобства.";

    static {
        try {
            LOGIN_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "email");
            PASSWORD_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "password");
            EQUALS_F = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "$eq");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Default constructor for actor.
     * Necessary for resolving from IOC.
     *
     * @param params - parameters for initialize actor.
     *               All parameters must not be a NULL or empty.
     * @see IUserAuthByLoginParams
     *
     * @throws InvalidArgumentException when given parameters is invalid.
     */
    public UserAuthByLoginActor(@Nonnull final IUserAuthByLoginParams params) throws InvalidArgumentException {
        try {
            checkParams(params);
            this.collection = params.getCollection();
            this.connectionPool = params.getConnectionPool();

            this.passwordEncoder = IOC.resolve(
                    Keys.getOrAdd("PasswordEncoder"),
                    params.getAlgorithm(),
                    params.getEncoder(),
                    params.getCharset()
            );
        } catch (ResolutionException | ReadValueException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Try to authenticate a some user by given login and password.
     * If authentication is successfully sets the status "SUCCESS" into the given auth message,
     *              else sets status "FAIL" and error message into the given auth message.
     *
     * @param message - message for authentication with user's login and password.
     *                The user's login and password must not be a NULL or empty.
     * @see IUserAuthByLoginMessage
     *
     * @throws AuthenticateUserException when:
     *                1. User with given login hasn't registered in the system.
     *                2. Given password is invalid.
     *                3. In the system has a few users with given login.
     *                4. In the system user with given login saved without password.
     *                5. A wrong work of search database task : task not prepare a query.
     */
    public void authenticateUser(@Nonnull final IUserAuthByLoginMessage message)
            throws AuthenticateUserException {
        try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
            checkMsg(message);
            IObject user = resolveLogin(message, poolGuard);
            validatePassword(message, user);
            setSuccessResponse(message);
        } catch (PoolGuardException | ReadValueException | ChangeValueException | InvalidArgumentException e) {
            try {
                setFailResponse(message, INTERNAL_ERROR_MSG);
            } catch (ChangeValueException ignored) { }
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private IObject resolveLogin(final IUserAuthByLoginMessage message, final IPoolGuard connectionPoolGuard)
        throws AuthenticateUserException {

        try {
            IObject searchQuery = prepareQueryParams(message);
            List<IObject> items = new LinkedList<>();
            ITask searchTask = IOC.resolve(
                Keys.getOrAdd("db.collection.search"),
                connectionPoolGuard.getObject(),
                collection,
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

            return items.get(0);
        } catch (ResolutionException | TaskExecutionException | InvalidArgumentException | ChangeValueException | ReadValueException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private void validateSearchResult(final List<IObject> searchResult, final IUserAuthByLoginMessage message)
        throws AuthenticateUserException {

        try {
            if (searchResult.isEmpty()) {
                setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
                throw new AuthenticateUserException(AUTH_ERROR_MSG +
                        "user with login: [" + message.getLogin() + "] doesn't exist!");
            }
            if (searchResult.size() > 1) {
                setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
                throw new AuthenticateUserException(AUTH_ERROR_MSG +
                        "too many users with login: [" + message.getLogin() + "]!");
            }
        } catch (ReadValueException | ChangeValueException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private void validatePassword(final IUserAuthByLoginMessage message, final IObject user)
            throws AuthenticateUserException {
        try {
            String password = PASSWORD_F.in(user);
            if (password == null || password.isEmpty()) {
                setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
                throw new AuthenticateUserException(AUTH_ERROR_MSG +
                        "user with login: [" + message.getLogin() + "] hasn't password!");
            }
            if (message.getPassword().equals(passwordEncoder.encode(password))) {
                setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
                throw new AuthenticateUserException(AUTH_ERROR_MSG +
                        "Invalid password: [" + message.getPassword() + "] for login: [" + message.getLogin() + "]!");
            }
        } catch (EncodingException | InvalidArgumentException | ReadValueException | ChangeValueException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private IObject prepareQueryParams(final IUserAuthByLoginMessage message)
        throws ResolutionException, ChangeValueException, InvalidArgumentException, ReadValueException {

        IObject filter = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject page = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject searchQuery = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        IField collectionNameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
        collectionNameF.out(searchQuery, this.collection);
        IField pageSizeF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "size");
        pageSizeF.out(page, 1);
        IField pageNumberF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "number");
        pageNumberF.out(page, 1);
        IField pageF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "page");
        pageF.out(searchQuery, page);
        IField filterF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "filter");
        filterF.out(searchQuery, filter);

        IObject loginObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        EQUALS_F.out(loginObject, message.getLogin());
        LOGIN_F.out(filter, loginObject);

        return searchQuery;
    }

    private void setSuccessResponse(final IUserAuthByLoginMessage message) throws ChangeValueException {
        message.setAuthStatus("SUCCESS");
        message.setAuthMessage("");
    }

    private void setFailResponse(final IUserAuthByLoginMessage message, final String errorMessage) throws ChangeValueException {
        message.setAuthStatus("FAIL");
        message.setAuthMessage(errorMessage);
    }

    private void checkParams(final IUserAuthByLoginParams params) throws InvalidArgumentException, ReadValueException {
        if (isNullOrEmpty(params.getCollection())) {
            throw new InvalidArgumentException("Invalid collection name!");
        }
        if (isNullOrEmpty(params.getAlgorithm())) {
            throw new InvalidArgumentException("Invalid algorithm for password encoding!");
        }
    }

    private void checkMsg(final IUserAuthByLoginMessage msg) throws InvalidArgumentException, ReadValueException, ChangeValueException {
        if (isNullOrEmpty(msg.getLogin()) || isNullOrEmpty(msg.getPassword())) {
            setFailResponse(msg, AUTH_ERROR_RESPONSE_MSG);
            throw new InvalidArgumentException("Invalid message format!");
        }
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
