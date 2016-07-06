/**
 * Contains CreateSessionActor
 */
package info.smart_tools.smartactrors.core.actrors.create_session;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
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
import info.smart_tools.smartactrors.core.actrors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactrors.core.actrors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactrors.core.actrors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactrors.core.actrors.create_session.wrapper.Session;

/**
 * Actor check current session, if she's null create new session
 */
public class CreateSessionActor {
    private String collectionName;
    private IPool connectionPool;

    private static Field<IObject> SESSION_ID_F;
    private static Field<String> EQUALS_F;

    static {
        try {
            SESSION_ID_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "sessionId"));
            EQUALS_F = new Field<>(IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "$eq"));

        } catch (ResolutionException e) {
            //TODO:: handle exception
        }
    }


    /**
     * Constructor for CreateSessionActor
     * @param config is any configurations
     */
    public CreateSessionActor(final CreateSessionConfig config) {
        try {
            this.collectionName = config.getCollectionName();
            this.connectionPool = config.getConnectionPool();
        } catch (ReadValueException | ChangeValueException e) {
            //TODO:: handle exception
        }
    }

    /**
     * Check current session, if she's null create new session
     * @param inputMessage message for checking
     * @throws CreateSessionException Calling when throws any exception inside CreateSessionActor
     */
    public void createSession(final CreateSessionMessage inputMessage) throws CreateSessionException {
        try {
            String sessionId = inputMessage.getSessionId();
            if (sessionId == null || sessionId.equals("")) {
                IObject authInfo = inputMessage.getAuthInfo();
                Session newSession = IOC.resolve(Keys.getOrAdd(Session.class.toString()));
                newSession.setAuthInfo(authInfo);
                inputMessage.setSession(newSession);
            } else {
                try (IPoolGuard poolGuard = new PoolGuard(connectionPool)) {
                    /*
                    DBSearchTask getSessionTask = IOC.resolve(Keys.getOrAdd(DBSearchTask.class.toString()));
                    if (getSessionTask == null) {
                        //TODO:: throw exception
                    }
                    QueryStatement queryStatement = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
                    Writer bodyWriter = queryStatement.getBodyWriter();
                    try {
                        bodyWriter.write(String.format("SELECT * FROM %s WHERE ", CollectionName.fromString(CollectionName.fromString(collectionName).toString())));
                        bodyWriter.write(String.format("sessionId = \'%s\'", sessionId));
                    } catch (IOException e) {
                        throw new CreateSessionException("Cannot fill query statement bodyWriter", e);
                    }
                    CompiledQuery compiledQuery = IOC.resolve(Keys.getOrAdd(CompiledQuery.class.toString()), connectionPool, queryStatement);
                */
                    IDatabaseTask searchTask = IOC.resolve(Keys.getOrAdd(IDatabaseTask.class.toString()), "PSQL");
                    IObject searchQuery = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

                } catch (PoolGuardException e) {
                    throw new CreateSessionException("Cannot get connection from pool.", e);
                } catch (QueryBuildException e) {
                    //TODO:: handle
                }
            }
        } catch (ReadValueException | ChangeValueException e) {
            //TODO:: handle exception
        } catch (ResolutionException e) {
            //TODO:: maybe throw new MessageHandleException
            throw new RuntimeException("Cannot resolve Session.class", e);
        }
    }

    private void prepareSearchQueay(final IObject searchQuery) {
        try {
            IObject query = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));
            IObject sessionIdObject = IOC.resolve(Keys.getOrAdd(IObject.class.toString()));

            sessionIdObject

            IFieldName collectionNameFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "collectionName");
            searchQuery.setValue(collectionNameFN, this.collectionName);
            IFieldName pageSizeFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "pageSize");
            searchQuery.setValue(pageSizeFN, 1);
            IFieldName pageNumber = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "pageNumber");
            searchQuery.setValue(pageNumber, 1);
            IFieldName bufferedQueryFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "bufferedQuery");
            searchQuery.setValue(bufferedQueryFN, bufferedQuery);
            IFieldName criteriaFN = IOC.resolve(Keys.getOrAdd(IFieldName.class.toString()), "criteria");
            searchQuery.setValue(criteriaFN, query);

        } catch (ResolutionException | ChangeValueException | InvalidArgumentException e) {
            //TODO:: handle exception
        }
    }
}
