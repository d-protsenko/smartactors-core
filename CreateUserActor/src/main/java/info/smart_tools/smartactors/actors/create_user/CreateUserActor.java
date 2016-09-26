package info.smart_tools.smartactors.actors.create_user;

import info.smart_tools.smartactors.actors.create_user.wrapper.MessageWrapper;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.postgres_connection.wrapper.ConnectionOptions;
import info.smart_tools.smartactors.security.encoding.encoders.EncodingException;
import info.smart_tools.smartactors.security.encoding.encoders.IPasswordEncoder;

/**
 * Actor for creating user
 */
public class CreateUserActor {
    private IPasswordEncoder passwordEncoder;
    private IPool connectionPool;
    private String collectionName;

    private IField collectionNameF;
    private IField userIdF;
    private IField passwordF;
    private IField algorithmF;
    private IField encoderF;
    private IField charsetF;

    /**
     * Constructor
     * @param params the actors params
     * @throws InvalidArgumentException Throw when can't read some value from message or resolving key or dependency is throw exception
     */
    public CreateUserActor(final IObject params) throws InvalidArgumentException {
        try {
            userIdF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "userId");
            passwordF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "пароль");
            collectionNameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");

            algorithmF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "algorithm");
            encoderF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "encoder");
            charsetF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "charset");

            ConnectionOptions connectionOptions = IOC.resolve(Keys.getOrAdd("PostgresConnectionOptions"));
            this.connectionPool = IOC.resolve(Keys.getOrAdd("PostgresConnectionPool"), connectionOptions);
            this.collectionName = collectionNameF.in(params);
            this.passwordEncoder = IOC.resolve(
                    Keys.getOrAdd("PasswordEncoder"),
                    algorithmF.in(params),
                    encoderF.in(params),
                    charsetF.in(params)
            );
        } catch (ResolutionException | ReadValueException e) {
            throw new InvalidArgumentException("Can't get key or resolve dependency", e);
        }
    }

    /**
     * Create a new user in collection
     * @param message the message
     * @throws TaskExecutionException Throw when can't get user or upsert his
     */
    public void create(final MessageWrapper message) throws TaskExecutionException {
        try {
            IObject user = message.getUser();
            userIdF.out(user, IOC.resolve(Keys.getOrAdd("db.collection.nextid")));
            passwordF.out(user, passwordEncoder.encode(passwordF.in(user)));

            try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                ITask searchTask = IOC.resolve(
                        Keys.getOrAdd("db.collection.upsert"),
                        poolGuard.getObject(),
                        collectionName,
                        user
                );
                searchTask.execute();
            }
        } catch (PoolGuardException e) {
            throw new TaskExecutionException("Failed to get connection", e);
        } catch (ResolutionException e) {
            throw new TaskExecutionException("Failed to resolve upsert task", e);
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new TaskExecutionException("Failed to get user object from message", e);
        } catch (EncodingException e) {
            throw new TaskExecutionException("Failed to encode password", e);
        }

    }
}
