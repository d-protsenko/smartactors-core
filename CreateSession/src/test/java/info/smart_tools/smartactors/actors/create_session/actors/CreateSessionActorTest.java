package info.smart_tools.smartactors.actors.create_session.actors;

import info.smart_tools.smartactors.actors.create_session.CreateSessionActor;
import info.smart_tools.smartactors.actors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.actors.create_session.wrapper.Session;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.wrapper_generator.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOC.class)
public class CreateSessionActorTest {
    private Session session;
    private CreateSessionMessage inputMessage;
    private IObject authInfo;
    CreateSessionActor actor;
    String collectionName = "collectionName";

    private IKey key;
    private IKey iObjectKey = mock(IKey.class);
    private IKey iFieldNameKey = mock(IKey.class);
    private IKey storageConnectionKey = mock(IKey.class);
    private IKey iDatabaseTaskKey = mock(IKey.class);
    private IKey fieldKey = mock(IKey.class);

    private Field<IObject> SESSION_ID_F;
    private Field<String> EQUALS_F;

    @org.junit.Before
    public void setUp() throws Exception {
        inputMessage = mock(CreateSessionMessage.class);
        session = mock(Session.class);
        authInfo = mock(IObject.class);
        when(inputMessage.getAuthInfo()).thenReturn(authInfo);

        mockStatic(IOC.class);
        key = mock(IKey.class);
        IKey sessionKey = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key);
        when(IOC.resolve(eq(key), eq("interface info.smart_tools.smartactors.actors.create_session.wrapper.Session"))).thenReturn(sessionKey);
        when(IOC.resolve(eq(sessionKey))).thenReturn(session);

        CreateSessionConfig config = mock(CreateSessionConfig.class);
        when(config.getCollectionName()).thenReturn(collectionName);
        IPool connectionPool = mock(IPool.class);
        when(config.getConnectionPool()).thenReturn(connectionPool);


        //IOC keys mock
        //TODO:: change path to Field when he's merged
        when(IOC.resolve(eq(key), eq("class info.smart_tools.smartactors.core.wrapper_generator.Field"))).thenReturn(fieldKey);
        when(IOC.resolve(eq(key), eq(IDatabaseTask.class.toString()))).thenReturn(iDatabaseTaskKey);
        when(IOC.resolve(eq(key), eq("interface info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection"))).thenReturn(storageConnectionKey);
        when(IOC.resolve(eq(key), eq("interface info.smart_tools.smartactors.core.iobject.IObject"))).thenReturn(iObjectKey);
        when(IOC.resolve(eq(key), eq("interface info.smart_tools.smartactors.core.iobject.IFieldName"))).thenReturn(iFieldNameKey);

        //mock constructor
        IFieldName SESSION_ID_FN = mock(IFieldName.class);
        IFieldName EQUALS_FN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("sessionId"))).thenReturn(SESSION_ID_FN);
        when(IOC.resolve(eq(iFieldNameKey), eq("$eq"))).thenReturn(EQUALS_FN);
        SESSION_ID_F = mock(Field.class);
        EQUALS_F = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(SESSION_ID_FN))).thenReturn(SESSION_ID_F);
        when(IOC.resolve(eq(fieldKey), eq(EQUALS_FN))).thenReturn(EQUALS_F);

        actor = new CreateSessionActor(config);
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdIsNull() throws ChangeValueException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn(null);
        actor.resolveSession(inputMessage);
        verify(session).setAuthInfo(eq(authInfo));
        verify(inputMessage).setSession(eq(session));
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdEqualEmptyString() throws CreateSessionException, ReadValueException, ChangeValueException {
        when(inputMessage.getSessionId()).thenReturn("");
        actor.resolveSession(inputMessage);
        verify(session).setAuthInfo(eq(authInfo));
        verify(inputMessage).setSession(eq(session));
    }

    @Test(expected = CreateSessionException.class)
    public void Should_trySearchSessionInDB_And_ThrowException_When_AnySearchQueryError() throws ResolutionException, TaskSetConnectionException, TaskPrepareException, TaskExecutionException, ChangeValueException, InvalidArgumentException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        StorageConnection connection = mock(StorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IFieldName bufferedQueryFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("bufferedQuery"))).thenReturn(bufferedQueryFN);

        Field<IObject> bufferedQueryF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(bufferedQueryFN))).thenReturn(bufferedQueryF);
        when(bufferedQueryF.out(eq(searchQuery))).thenReturn(null);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).in(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).in(query, sessionIdIObject);
        IFieldName collectionNameFN = mock(IFieldName.class);
        IFieldName pageSizeFN = mock(IFieldName.class);
        IFieldName pageNumberFN = mock(IFieldName.class);
        IFieldName criteriaFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("collectionName"))).thenReturn(collectionNameFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageSize"))).thenReturn(pageSizeFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageNumber"))).thenReturn(pageNumberFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("criteria"))).thenReturn(criteriaFN);

        actor.resolveSession(inputMessage);
    }

    @Test(expected = CreateSessionException.class)
    public void Should_ThrowException_When_CountOfSearchResultIsNull() throws ResolutionException, TaskSetConnectionException, TaskPrepareException, TaskExecutionException, ChangeValueException, InvalidArgumentException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        StorageConnection connection = mock(StorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject bufferedQuery = mock(IObject.class);
        IFieldName bufferedQueryFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("bufferedQuery"))).thenReturn(bufferedQueryFN);
        Field<IObject> bufferedQueryF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(bufferedQueryFN))).thenReturn(bufferedQueryF);
        when(bufferedQueryF.out(eq(searchQuery))).thenReturn(bufferedQuery);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).in(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).in(query, sessionIdIObject);
        IFieldName collectionNameFN = mock(IFieldName.class);
        IFieldName pageSizeFN = mock(IFieldName.class);
        IFieldName pageNumberFN = mock(IFieldName.class);
        IFieldName criteriaFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("collectionName"))).thenReturn(collectionNameFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageSize"))).thenReturn(pageSizeFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageNumber"))).thenReturn(pageNumberFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("criteria"))).thenReturn(criteriaFN);

        IFieldName countSearchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("countSearchResult"))).thenReturn(countSearchResultFN);
        Field<Integer> countSearchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(countSearchResultFN))).thenReturn(countSearchResultF);
        when(countSearchResultF.out(searchQuery)).thenReturn(0);

        actor.resolveSession(inputMessage);

        verify(searchTask).setConnection(connection);
        verify(searchTask).prepare(searchQuery);
        verify(searchTask).execute();
        verify(searchQuery).setValue(collectionNameFN, this.collectionName);
        verify(searchQuery).setValue(pageSizeFN, 1);
        verify(searchQuery).setValue(pageNumberFN, 1);
        verify(searchQuery).setValue(bufferedQueryFN, bufferedQueryFN);

    }

    @Test(expected = CreateSessionException.class)
    public void Should_ThrowException_When_SearchResultIsEmptyList() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        StorageConnection connection = mock(StorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject bufferedQuery = mock(IObject.class);
        IFieldName bufferedQueryFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("bufferedQuery"))).thenReturn(bufferedQueryFN);
        Field<IObject> bufferedQueryF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(bufferedQueryFN))).thenReturn(bufferedQueryF);
        when(bufferedQueryF.out(eq(searchQuery))).thenReturn(bufferedQuery);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).in(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).in(query, sessionIdIObject);
        IFieldName collectionNameFN = mock(IFieldName.class);
        IFieldName pageSizeFN = mock(IFieldName.class);
        IFieldName pageNumberFN = mock(IFieldName.class);
        IFieldName criteriaFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("collectionName"))).thenReturn(collectionNameFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageSize"))).thenReturn(pageSizeFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageNumber"))).thenReturn(pageNumberFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("criteria"))).thenReturn(criteriaFN);

        IFieldName countSearchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("countSearchResult"))).thenReturn(countSearchResultFN);
        Field<Integer> countSearchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(countSearchResultFN))).thenReturn(countSearchResultF);
        when(countSearchResultF.out(searchQuery)).thenReturn(1);

        IFieldName searchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("searchResult"))).thenReturn(searchResultFN);
        Field<List<IObject>> searchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(searchResultFN))).thenReturn(searchResultF);
        when(searchResultF.out(searchQuery)).thenReturn(Collections.emptyList());

        actor.resolveSession(inputMessage);

        verify(searchTask).setConnection(connection);
        verify(searchTask).prepare(searchQuery);
        verify(searchTask).execute();
        verify(searchQuery).setValue(collectionNameFN, this.collectionName);
        verify(searchQuery).setValue(pageSizeFN, 1);
        verify(searchQuery).setValue(pageNumberFN, 1);
        verify(searchQuery).setValue(bufferedQueryFN, bufferedQueryFN);

    }

    @Test(expected = CreateSessionException.class)
    public void Should_searchSessionInDB_When_FoundSessionIsNull() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        StorageConnection connection = mock(StorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject bufferedQuery = mock(IObject.class);
        IFieldName bufferedQueryFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("bufferedQuery"))).thenReturn(bufferedQueryFN);
        Field<IObject> bufferedQueryF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(bufferedQueryFN))).thenReturn(bufferedQueryF);
        when(bufferedQueryF.out(eq(searchQuery))).thenReturn(bufferedQuery);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).in(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).in(query, sessionIdIObject);
        IFieldName collectionNameFN = mock(IFieldName.class);
        IFieldName pageSizeFN = mock(IFieldName.class);
        IFieldName pageNumberFN = mock(IFieldName.class);
        IFieldName criteriaFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("collectionName"))).thenReturn(collectionNameFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageSize"))).thenReturn(pageSizeFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageNumber"))).thenReturn(pageNumberFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("criteria"))).thenReturn(criteriaFN);

        IFieldName countSearchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("countSearchResult"))).thenReturn(countSearchResultFN);
        Field<Integer> countSearchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(countSearchResultFN))).thenReturn(countSearchResultF);
        when(countSearchResultF.out(searchQuery)).thenReturn(1);

        IFieldName searchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("searchResult"))).thenReturn(searchResultFN);
        Field<List<IObject>> searchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(searchResultFN))).thenReturn(searchResultF);
        IObject result = mock(IObject.class);
        when(searchResultF.out(searchQuery)).thenReturn(Collections.singletonList(result));

        IFieldName sessionFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("session"))).thenReturn(sessionFN);
        Field<Session> sessionF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(sessionFN))).thenReturn(sessionF);
        when(sessionF.out(result)).thenReturn(null);

        actor.resolveSession(inputMessage);

        verify(searchTask).setConnection(connection);
        verify(searchTask).prepare(searchQuery);
        verify(searchTask).execute();
        verify(searchQuery).setValue(collectionNameFN, this.collectionName);
        verify(searchQuery).setValue(pageSizeFN, 1);
        verify(searchQuery).setValue(pageNumberFN, 1);
        verify(searchQuery).setValue(bufferedQueryFN, bufferedQueryFN);

    }

    @Test
    public void Should_searchSessionInDB_When_SessionIdIsNotNullAndNotEmptyString() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        StorageConnection connection = mock(StorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject bufferedQuery = mock(IObject.class);
        IFieldName bufferedQueryFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("bufferedQuery"))).thenReturn(bufferedQueryFN);
        Field<IObject> bufferedQueryF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(bufferedQueryFN))).thenReturn(bufferedQueryF);
        when(bufferedQueryF.out(eq(searchQuery))).thenReturn(bufferedQuery);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).in(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).in(query, sessionIdIObject);
        IFieldName collectionNameFN = mock(IFieldName.class);
        IFieldName pageSizeFN = mock(IFieldName.class);
        IFieldName pageNumberFN = mock(IFieldName.class);
        IFieldName criteriaFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("collectionName"))).thenReturn(collectionNameFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageSize"))).thenReturn(pageSizeFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("pageNumber"))).thenReturn(pageNumberFN);
        when(IOC.resolve(eq(iFieldNameKey), eq("criteria"))).thenReturn(criteriaFN);

        IFieldName countSearchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("countSearchResult"))).thenReturn(countSearchResultFN);
        Field<Integer> countSearchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(countSearchResultFN))).thenReturn(countSearchResultF);
        when(countSearchResultF.out(searchQuery)).thenReturn(1);

        IFieldName searchResultFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("searchResult"))).thenReturn(searchResultFN);
        Field<List<IObject>> searchResultF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(searchResultFN))).thenReturn(searchResultF);
        IObject result = mock(IObject.class);
        when(searchResultF.out(searchQuery)).thenReturn(Collections.singletonList(result));

        IFieldName sessionFN = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), eq("session"))).thenReturn(sessionFN);
        Field<Session> sessionF = mock(Field.class);
        when(IOC.resolve(eq(fieldKey), eq(sessionFN))).thenReturn(sessionF);
        Session sessionFromDB = mock(Session.class);
        when(sessionF.out(result)).thenReturn(sessionFromDB);

        actor.resolveSession(inputMessage);

        verify(inputMessage).setSession(eq(sessionFromDB));
        verify(searchTask).setConnection(connection);
        verify(searchTask).prepare(searchQuery);
        verify(searchTask).execute();
        verify(searchQuery).setValue(collectionNameFN, this.collectionName);
        verify(searchQuery).setValue(pageSizeFN, 1);
        verify(searchQuery).setValue(pageNumberFN, 1);
        //in private method bufferedQuery is null always
        verify(searchQuery).setValue(bufferedQueryFN, null);
    }
}