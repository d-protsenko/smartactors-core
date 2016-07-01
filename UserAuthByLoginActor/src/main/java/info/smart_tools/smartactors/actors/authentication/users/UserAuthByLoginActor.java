package info.smart_tools.smartactors.actors.authentication.users;

import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMessage;
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
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.security.encoding.codecs.ICharSequenceCodec;
import info.smart_tools.smartactors.core.security.encoding.encoders.EncodingException;
import info.smart_tools.smartactors.core.security.encoding.encoders.IEncoder;
import info.smart_tools.smartactors.core.security.encoding.encoders.IPasswordEncoder;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
import info.smart_tools.smartactors.core.wrapper_generator.IObjectWrapper;

/**
 *
 */
public class UserAuthByLoginActor {
    private String collection;
    private IBufferedQuery bufferedQuery;
    private IPasswordEncoder passwordEncoder;

    private static final Field<IObject> LOGIN_F;
    private static final Field<String> PASSWORD_F;
    private static final Field<String> EQUALS_F;

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
        try {
            checkParams(params);

            this.collection = params.getCollection();

            IEncoder encoder = IOC.resolve(Keys.getOrAdd(IEncoder.class.toString() + "<-Hex"));
            ICharSequenceCodec charSequenceCodec = IOC.resolve(
                    Keys.getOrAdd(ICharSequenceCodec.class.toString() + "<-UTF-8"));
            this.passwordEncoder = IOC.resolve(
                    Keys.getOrAdd(IPasswordEncoder.class.toString() + "<-MDPassword"),
                    params.getAlgorithmEncode(),
                    encoder,
                    charSequenceCodec);
        } catch (ResolutionException e) {
            throw new InvalidArgumentException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param message
     * @throws InvalidArgumentException
     * @throws AuthenticateUserException
     */
    public void authenticateUser(final IUserAuthByLoginMessage message)
            throws InvalidArgumentException, AuthenticateUserException {
        try (IPoolGuard poolGuard = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"))) {
            checkMsg(message);
            IObject user = validateLogin(message, poolGuard);
            validatePassword(message, user);
            setSuccessResponse(message);
        } catch (ResolutionException e) {
            setFailResponse(message, INTERNAL_ERROR_MSG);
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private IObject validateLogin(final IUserAuthByLoginMessage message, final IPoolGuard poolGuard)
            throws AuthenticateUserException {
        try {
            IDatabaseTask searchTask = IOC.resolve(Keys.getOrAdd(PSQLSearchTask.class.toString()));
            ISearchQuery searchQuery = IOC.resolve(Keys.getOrAdd(ISearchQuery.class.toString()));

            searchTask.setConnection((StorageConnection) poolGuard.getObject());
            searchTask.prepare(prepareQueryMsg(searchQuery, message));
            searchTask.execute();

            this.bufferedQuery = searchQuery
                    .getBufferedQuery()
                    .orElseThrow(() -> new AuthenticateUserException("Search task didn't returned a buffered query!"));

            if (searchQuery.countSearchResult() == 0) {
                setFailResponse(message, AUTH_ERROR_RESPONSE_MSG);
                throw new AuthenticateUserException(AUTH_ERROR_MSG +
                        "user with login: [" + message.getLogin() + "] doesn't exist!");
            }

            return searchQuery.getSearchResult(0);
        } catch (ResolutionException | InvalidArgumentException | ChangeValueException |
                TaskSetConnectionException | TaskExecutionException | TaskPrepareException e) {
            throw new AuthenticateUserException(AUTH_ERROR_MSG + e.getMessage(), e);
        }
    }

    private void validatePassword(final IUserAuthByLoginMessage message, final IObject user)
            throws AuthenticateUserException {
        try {
            String password = PASSWORD_F.from(user, String.class);
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

    private IObject prepareQueryMsg(final ISearchQuery searchQuery, final IUserAuthByLoginMessage message)
            throws ResolutionException, ChangeValueException, InvalidArgumentException {

        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
        IObject loginObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

        EQUALS_F.inject(loginObject, message.getLogin());
        LOGIN_F.inject(query, loginObject);

        searchQuery.setCollectionName(collection);
        searchQuery.setPageSize(1);
        searchQuery.setPageNumber(1);
        searchQuery.setBufferedQuery(bufferedQuery);
        searchQuery.setCriteria(query);

        IObject[] initObjects = IOC.resolve(
                Keys.getOrAdd(IObjectWrapper.class.toString() + ".getIObjects"),
                searchQuery);

        return initObjects[0];
    }

    private void setSuccessResponse(final IUserAuthByLoginMessage message) {
        message.setAuthStatus("SUCCESS");
        message.setAuthMessage("");
    }

    private void setFailResponse(final IUserAuthByLoginMessage message, final String errorMessage) {
        message.setAuthStatus("SUCCESS");
        message.setAuthMessage(errorMessage);
    }

    private void checkParams(final IUserAuthByLoginParams params) throws InvalidArgumentException {
        if (isNullOrEmpty(params.getCollection())) {
            throw new InvalidArgumentException("Invalid collection name!");
        }
        if (isNullOrEmpty(params.getAlgorithmEncode())) {
            throw new InvalidArgumentException("Invalid algorithm for password encoding!");
        }
    }

    private void checkMsg(final IUserAuthByLoginMessage msg) throws InvalidArgumentException {
        if (isNullOrEmpty(msg.getLogin()) || isNullOrEmpty(msg.getPassword())) {
            throw new InvalidArgumentException("Invalid message format!");
        }
    }

    private boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
