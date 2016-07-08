/**
 * Contains CreateSessionActor
 */
package info.smart_tools.smartactors.actors.create_session;

import info.smart_tools.smartactors.actors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.actors.create_session.wrapper.Session;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.wrapper_generator.Field;

import java.util.List;

/**
 * Actor check current session, if she's null create new session
 */
public class CreateSessionActor {
    private String collectionName;
    private IPool connectionPool;
    //TODO:: in future will change dependency from core.db_search_task to core.db_task
    private IObject bufferedQuery;

    private static Field<IObject> SESSION_ID_F;
    private static Field<String> EQUALS_F;

    static {
        try {
            IFieldName SESSION_ID_FN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "sessionId");
            SESSION_ID_F = IOC.resolve(Keys.getOrAdd(Field.class.toString()), SESSION_ID_FN);
            IFieldName EQUALS_FN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "$eq");
            EQUALS_F = IOC.resolve(Keys.getOrAdd(Field.class.toString()), EQUALS_FN);

        } catch (ResolutionException e) {
            //TODO:: handle exception
        }
    }

    /**
     * Constructor for CreateSessionActor
     * @param config is any configurations
     * @throws CreateSessionException for any occurred error
     */
    public CreateSessionActor(final CreateSessionConfig config) throws CreateSessionException {
        //TODO:: replace CreateSessionConfig to IObject
        try {
            this.collectionName = config.getCollectionName();
            this.connectionPool = config.getConnectionPool();
        } catch (ReadValueException | ChangeValueException e) {
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
                Session newSession = IOC.resolve(Keys.getOrAdd(Session.class.toString()));
                newSession.setAuthInfo(authInfo);
                inputMessage.setSession(newSession);
            } else {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    IDatabaseTask searchTask = IOC.resolve(Keys.getOrAdd(IDatabaseTask.class.toString()), "PSQL");
                    IObject searchQuery = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

                    StorageConnection connection = IOC.resolve(Keys.getOrAdd(StorageConnection.class.toString()), poolGuard.getObject());
                    prepareSearchQuery(searchQuery, inputMessage);

                    searchTask.setConnection(connection);
                    searchTask.prepare(searchQuery);
                    searchTask.execute();

                    IFieldName bufferedQueryFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "bufferedQuery");
                    Field<IObject> bufferedQueryF = IOC.resolve(Keys.getOrAdd(Field.class.toString()), bufferedQueryFN);
                    try {
                        this.bufferedQuery = bufferedQueryF.out(searchQuery);
                        if (bufferedQuery == null) {
                            throw new CreateSessionException("Search Query is null.Search task didn't returned a buffered query!");
                        }
                    } catch (InvalidArgumentException e) {
                        throw new CreateSessionException("Search task didn't returned a buffered query!", e);
                    }

                    IFieldName countSearchResultFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "countSearchResult");
                    Field<Integer> countSearchResultF = IOC.resolve(Keys.getOrAdd(Field.class.toString()), countSearchResultFN);
                    if (countSearchResultF.out(searchQuery) == 0) {
                        throw new CreateSessionException("Cannot find session by sessionId: "
                                + inputMessage.getSessionId()
                        );
                    }

                    IFieldName searchResultFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "searchResult");
                    Field<List<IObject>> searchResultField = IOC.resolve(Keys.getOrAdd(Field.class.toString()), searchResultFN);
                    IObject result = searchResultField.out(searchQuery).get(0);

                    IFieldName sessionFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "session");
                    Field<Session> sessionF = IOC.resolve(Keys.getOrAdd(Field.class.toString()), sessionFN);
                    Session fromDBSession = sessionF.out(result);
                    if (fromDBSession == null) {
                        throw new CreateSessionException("Find session is null");
                    }
                    inputMessage.setSession(fromDBSession);

                } catch (PoolGuardException e) {
                    throw new CreateSessionException("Cannot get connection from pool.", e);
                } catch (Exception e) {
                    throw new CreateSessionException("Error during find session by sessionId: " + inputMessage.getSessionId(), e);
                }
            }
        } catch (ReadValueException | ChangeValueException e) {
            throw new CreateSessionException("Cannot create or find session by sessionId", e);
        } catch (ResolutionException e) {
            throw new CreateSessionException("Error because cannot resolve Session.class", e);
        }
    }

    private void prepareSearchQuery(final IObject searchQuery, final CreateSessionMessage inputMessage)
            throws ChangeValueException, InvalidArgumentException, ResolutionException, ReadValueException {
        IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
        IObject sessionIdObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

        EQUALS_F.in(sessionIdObject, inputMessage.getSessionId());
        SESSION_ID_F.in(query, sessionIdObject);

        IFieldName collectionNameFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "collectionName");
        searchQuery.setValue(collectionNameFN, this.collectionName);
        IFieldName pageSizeFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "pageSize");
        searchQuery.setValue(pageSizeFN, 1);
        IFieldName pageNumberFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "pageNumber");
        searchQuery.setValue(pageNumberFN, 1);
        IFieldName bufferedQueryFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "bufferedQuery");
        searchQuery.setValue(bufferedQueryFN, bufferedQuery);
        IFieldName criteriaFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "criteria");
        searchQuery.setValue(criteriaFN, query);

    }
}
