/**
 * Contains CreateSessionActor
 */
package info.smart_tools.smartactors.actors.create_session;

import info.smart_tools.smartactors.actors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
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
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Actor check current session, if she's null create new session
 */
public class CreateSessionActor {
    private String collectionName;
    private IPool connectionPool;

    private IField collectionNameF;
    private IField pageSizeF;
    private IField pageNumberF;
    private IField pageF;
    private IField filterF;

    private IField sessionIdF;
    private IField equalsF;
    private IField authInfoF;

//    static {
//        try {
//
//
//
//        } catch (ResolutionException e) {
//           throw new RuntimeException(e);
//        }
//    }

    /**
     * Constructor for CreateSessionActor
     * @param config is any configurations
     * @throws CreateSessionException for any occurred error
     */
    public CreateSessionActor(final CreateSessionConfig config) throws CreateSessionException {
        try {
            collectionNameF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            pageSizeF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "size");
            pageNumberF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "number");
            pageF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "page");
            filterF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "filter");

            sessionIdF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "sessionId");
            equalsF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "$eq");
            authInfoF = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "authInfo");

            this.collectionName = config.getCollectionName();
            this.connectionPool = config.getConnectionPool();
        } catch (Exception e) {
            throw new CreateSessionException("Can't create Actor");
        }
    }

    /**
     * Check current session, if she's null create new session
     * @param inputMessage message for checking
     * @throws CreateSessionException Calling when throws any exception inside CreateSessionActor
     */
    public void resolveSession(final CreateSessionMessage inputMessage) throws CreateSessionException {
        try {
            String sessionId = inputMessage.getSessionId();
            if (sessionId == null || sessionId.equals("")) {
                IObject authInfo = inputMessage.getAuthInfo();
                IObject newSession = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
                authInfoF.out(newSession, authInfo);
                inputMessage.setSession(newSession);
            } else {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    IObject searchQuery = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                    //TODO:: resolve with IOC
                    IStorageConnection connection = (IStorageConnection) poolGuard.getObject();
                    prepareSearchQuery(searchQuery, inputMessage);

                    List<IObject> items = new LinkedList<>();
                    ITask searchTask = IOC.resolve(
                        Keys.getOrAdd("db.collection.search"),
                        connection,
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

                    if (items.isEmpty()) {
                        //TODO:: Should we create new session here?
                        return;
                    }
                    IObject result = items.get(0);
                    if (result == null) {
                        throw new CreateSessionException("Find session is null");
                    }
                    inputMessage.setSession(result);

                } catch (PoolGuardException e) {
                    throw new CreateSessionException("Cannot get connection from pool.", e);
                } catch (Exception e) {
                    throw new CreateSessionException("Error during find session by sessionId: " + inputMessage.getSessionId(), e);
                }
            }
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException e) {
            throw new CreateSessionException("Cannot create or find session by sessionId", e);
        } catch (ResolutionException e) {
            throw new CreateSessionException("Resolution error", e);
        }
    }

    private void prepareSearchQuery(final IObject searchQuery, final CreateSessionMessage inputMessage)
            throws ChangeValueException, InvalidArgumentException, ResolutionException, ReadValueException {
        IObject filter = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject sessionIdObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        IObject page = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        equalsF.out(sessionIdObject, inputMessage.getSessionId());
        sessionIdF.out(filter, sessionIdObject);

        collectionNameF.out(searchQuery, this.collectionName);
        pageSizeF.out(page, 1);
        pageNumberF.out(page, 1);
        pageF.out(searchQuery, page);

        filterF.out(searchQuery, filter);

    }
}
