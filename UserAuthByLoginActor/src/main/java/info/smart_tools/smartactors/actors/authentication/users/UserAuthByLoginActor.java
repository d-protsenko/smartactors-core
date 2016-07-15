package info.smart_tools.smartactors.actors.authentication.users;

import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMessage;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_task.search.utils.IBufferedQuery;
import info.smart_tools.smartactors.core.db_task.search.wrappers.ISearchQuery;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.security.encoding.codecs.ICharSequenceCodec;
import info.smart_tools.smartactors.core.security.encoding.encoders.EncodingException;
import info.smart_tools.smartactors.core.security.encoding.encoders.IEncoder;
import info.smart_tools.smartactors.core.security.encoding.encoders.IPasswordEncoder;

import javax.annotation.Nonnull;

/**
 * Actor for authenticate users by given login and password.
 *
 */
public class UserAuthByLoginActor {
    /** Name of a some collection in database where is users documents. */
    private String collection;

    /** Encoder for obtaining a some hash of given user's password. */
    private IPasswordEncoder passwordEncoder;

    private final IObject CRITERIA;
    private final IObject PARAMETERS;
    private final ISearchQuery SEARCH_QUERY;

    private final IField LOGIN_F;
    private final IField PASSWORD_F;

    /* ToDo : Needs message source. */
    private static final String AUTH_ERROR_MSG = "User authentication has been failed because: ";

    private static final String AUTH_ERROR_RESPONSE_MSG = "Такой почтовый адрес не зарегистрирован, либо пароль неверный. " +
            "Если Вы уверены, что регистрировались на нашем сайте, но не помните пароль, " +
            "то попробуйте его восстановить ниже по форме. Просто введите свой электронный адрес, " +
            "и Вам на электронную почту придет ссылка. Пройдя по ссылке, " +
            "Вы сможете ввести новый удобный для Вас пароль.";

    private static final String INTERNAL_ERROR_MSG = "Во время обработки запроса произошла ошибка. " +
            "Пожалуйста попробуйте повторить операцию. Приносим свои извинения за доставленные неудобства.";

    {
        try {
            LOGIN_F = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "email");
            PASSWORD_F = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "password");
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

            // Init criteria and parameters objects.
            CRITERIA = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            PARAMETERS = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            SEARCH_QUERY = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()), PARAMETERS);

            // Const. parameters in this search query.
            SEARCH_QUERY.setCollectionName(collection);
            SEARCH_QUERY.setPageSize(1);
            SEARCH_QUERY.setPageNumber(1);
            SEARCH_QUERY.setCriteria(CRITERIA);

            // Init. criteria for search query by login.
            IField EQUALS_F = IOC.resolve(Keys.getOrAdd(IField.class.toString()), "$eq");
            IObject loginObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            EQUALS_F.out(loginObject, "email");
            LOGIN_F.out(CRITERIA, loginObject);

            this.collection = params.getCollection();

            IEncoder encoder = IOC.resolve(Keys.getOrAdd(IEncoder.class.toString() + "<-" + params.getEncoder()));
            ICharSequenceCodec charSequenceCodec = IOC.resolve(
                    Keys.getOrAdd(ICharSequenceCodec.class.toString() + "<-" + params.getCharset()));
            this.passwordEncoder = IOC.resolve(
                    Keys.getOrAdd(IPasswordEncoder.class.toString() + "<-MDPassword"),
                    params.getAlgorithm(),
                    encoder,
                    charSequenceCodec);

        } catch (ResolutionException | ChangeValueException e) {
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
     * @throws InvalidArgumentException when given message is invalid.
     * @throws AuthenticateUserException when:
     *                1. User with given login hasn't registered in the system.
     *                2. Given password is invalid.
     *                3. In the system has a few users with given login.
     *                4. In the system user with given login saved without password.
     *                5. A wrong work of search database task : task not prepare a query.
     */
    public void authenticateUser(@Nonnull final IUserAuthByLoginMessage message)
            throws InvalidArgumentException, AuthenticateUserException {
        try (IPoolGuard connectionPoolGuard = IOC.resolve(Keys.getOrAdd("PSQLConnectionPoolGuard"));
                IPoolGuard taskPollGuard = IOC.resolve(Keys.getOrAdd("PSQLSearchTaskPoolGuard"))) {
            checkMsg(message);
            IObject user = resolveLogin(message, connectionPoolGuard, taskPollGuard);
            validatePassword(message, user);
            setSuccessResponse(message);
        } catch (ResolutionException e) {
            setFailResponse(message, INTERNAL_ERROR_MSG);
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private IObject resolveLogin(final IUserAuthByLoginMessage message,
                                 final IPoolGuard connectionPoolGuard,
                                 final IPoolGuard taskPoolGuard
    ) throws AuthenticateUserException {
        try {
            IDatabaseTask searchTask = (IDatabaseTask) taskPoolGuard.getObject();

            searchTask.setConnection((StorageConnection) connectionPoolGuard.getObject());
            searchTask.prepare(prepareQueryParams(message));
            searchTask.execute();

            validateSearchResult(SEARCH_QUERY, message);

            return SEARCH_QUERY.getSearchResult(0);
        } catch (ResolutionException | InvalidArgumentException | ChangeValueException |
                TaskSetConnectionException | TaskExecutionException | TaskPrepareException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private void validateSearchResult(final ISearchQuery searchQuery, final IUserAuthByLoginMessage message)
            throws AuthenticateUserException {
        if (searchQuery.countSearchResult() == 0) {
            setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
            throw new AuthenticateUserException(AUTH_ERROR_MSG +
                    "user with login: [" + message.getLogin() + "] doesn't exist!");
        }
        if (searchQuery.countSearchResult() > 1) {
            setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
            throw new AuthenticateUserException(AUTH_ERROR_MSG +
                    "too many users with login: [" + message.getLogin() + "]!");
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
        } catch (EncodingException | InvalidArgumentException | ReadValueException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    // ToDo :: Add setParameters after feature/143_db_tasks_refactoring merge.
    private IObject prepareQueryParams(final IUserAuthByLoginMessage message)
            throws ResolutionException, ChangeValueException, InvalidArgumentException {
        LOGIN_F.out(PARAMETERS, message.getLogin());
        // SEARCH_QUERY.setParameters(PARAMETERS);

        return PARAMETERS;
    }

    private void setSuccessResponse(final IUserAuthByLoginMessage message) {
        message.setAuthStatus("SUCCESS");
        message.setAuthMessage("");
    }

    private void setFailResponse(final IUserAuthByLoginMessage message, final String errorMessage) {
        message.setAuthStatus("FAIL");
        message.setAuthMessage(errorMessage);
    }

    private void checkParams(final IUserAuthByLoginParams params) throws InvalidArgumentException {
        if (isNullOrEmpty(params.getCollection())) {
            throw new InvalidArgumentException("Invalid collection name!");
        }
        if (isNullOrEmpty(params.getAlgorithm())) {
            throw new InvalidArgumentException("Invalid algorithm for password encoding!");
        }
    }

    private void checkMsg(final IUserAuthByLoginMessage msg) throws InvalidArgumentException {
        if (isNullOrEmpty(msg.getLogin()) || isNullOrEmpty(msg.getPassword())) {
            setFailResponse(msg, AUTH_ERROR_RESPONSE_MSG);
            throw new InvalidArgumentException("Invalid message format!");
        }
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
