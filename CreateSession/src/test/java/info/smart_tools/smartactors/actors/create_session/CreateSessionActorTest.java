package info.smart_tools.smartactors.actors.create_session;

import info.smart_tools.smartactors.actors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, CreateSessionActor.class})
public class CreateSessionActorTest {
    private CreateSessionMessage inputMessage;
    private IObject authInfo;
    private CreateSessionActor actor;
    private String collectionName = "collectionName";
    private IPool connectionPool;

    private IKey iObjectKey = mock(IKey.class);
    private IKey iFieldNameKey = mock(IKey.class);
    private IKey storageConnectionKey = mock(IKey.class);
    private IKey iDatabaseTaskKey = mock(IKey.class);
    private IKey fieldKey = mock(IKey.class);

    private IField SESSION_ID_F;
    private IField EQUALS_F;
    private IField AUTH_INFO_F;

    private IField collectionNameF;
    private IField sizeF;
    private IField numberF;
    private IField pageF;
    private IField filterF;

    @Before
    public void setUp() throws Exception {
        inputMessage = mock(CreateSessionMessage.class);
        authInfo = mock(IObject.class);
        when(inputMessage.getAuthInfo()).thenReturn(authInfo);

        mockStatic(IOC.class);
        IKey key = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key);

        CreateSessionConfig config = mock(CreateSessionConfig.class);
        when(config.getCollectionName()).thenReturn(collectionName);
        connectionPool = mock(IPool.class);
        when(config.getConnectionPool()).thenReturn(connectionPool);

        //IOC keys mock
        when(IOC.resolve(eq(key), eq(IField.class.getCanonicalName()))).thenReturn(fieldKey);
        when(IOC.resolve(eq(key), eq("db.collection.search"))).thenReturn(iDatabaseTaskKey);
        when(IOC.resolve(eq(key), eq(IStorageConnection.class.getCanonicalName()))).thenReturn(storageConnectionKey);
        when(IOC.resolve(eq(key), eq(IObject.class.getCanonicalName()))).thenReturn(iObjectKey);
        when(IOC.resolve(eq(key), eq(IFieldName.class.getCanonicalName()))).thenReturn(iFieldNameKey);

        //mock constructor
        SESSION_ID_F = mock(IField.class);
        EQUALS_F = mock(IField.class);
        AUTH_INFO_F = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("sessionId"))).thenReturn(SESSION_ID_F);
        when(IOC.resolve(eq(fieldKey), eq("$eq"))).thenReturn(EQUALS_F);
        when(IOC.resolve(eq(fieldKey), eq("authInfo"))).thenReturn(AUTH_INFO_F);

        collectionNameF = mock(IField.class);
        sizeF = mock(IField.class);
        numberF = mock(IField.class);
        pageF = mock(IField.class);
        filterF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("collectionName"))).thenReturn(collectionNameF);
        when(IOC.resolve(eq(fieldKey), eq("size"))).thenReturn(sizeF);
        when(IOC.resolve(eq(fieldKey), eq("number"))).thenReturn(numberF);
        when(IOC.resolve(eq(fieldKey), eq("page"))).thenReturn(pageF);
        when(IOC.resolve(eq(fieldKey), eq("filter"))).thenReturn(filterF);

        actor = new CreateSessionActor(config);
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdIsNull() throws Exception {
        when(inputMessage.getSessionId()).thenReturn(null);
        IObject sessionObj = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(sessionObj);
        actor.resolveSession(inputMessage);
        verify(inputMessage).setSession(any(IObject.class));
    }

    @Test(expected = CreateSessionException.class)
    public void Should_trySearchSessionInDB_And_ThrowException_When_AnySearchQueryError() throws ResolutionException, TaskSetConnectionException, TaskPrepareException, TaskExecutionException, ChangeValueException, InvalidArgumentException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).out(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).out(query, sessionIdIObject);
        IField collectionNameF = mock(IField.class);
        IField pageSizeF = mock(IField.class);
        IField pageNumberF = mock(IField.class);
        IField criteriaF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("collectionName"))).thenReturn(collectionNameF);
        when(IOC.resolve(eq(fieldKey), eq("pageSize"))).thenReturn(pageSizeF);
        when(IOC.resolve(eq(fieldKey), eq("pageNumber"))).thenReturn(pageNumberF);
        when(IOC.resolve(eq(fieldKey), eq("criteria"))).thenReturn(criteriaF);

        actor.resolveSession(inputMessage);
    }

    @Test(expected = CreateSessionException.class)
    public void Should_ThrowException_When_CountOfSearchResultIsNull() throws ResolutionException, TaskSetConnectionException, TaskPrepareException, TaskExecutionException, ChangeValueException, InvalidArgumentException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).out(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).out(query, sessionIdIObject);
        IField collectionNameF = mock(IField.class);
        IField pageSizeF = mock(IField.class);
        IField pageNumberF = mock(IField.class);
        IField criteriaF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("collectionName"))).thenReturn(collectionNameF);
        when(IOC.resolve(eq(fieldKey), eq("pageSize"))).thenReturn(pageSizeF);
        when(IOC.resolve(eq(fieldKey), eq("pageNumber"))).thenReturn(pageNumberF);
        when(IOC.resolve(eq(fieldKey), eq("criteria"))).thenReturn(criteriaF);

        IField countSearchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("countSearchResult"))).thenReturn(countSearchResultF);
        when(countSearchResultF.in(searchQuery)).thenReturn(0);

        actor.resolveSession(inputMessage);

        verify(searchTask).execute();

        verify(collectionNameF).out(searchQuery, collectionName);
        verify(pageSizeF).out(searchQuery, 1);
        verify(pageNumberF).out(searchQuery, 1);
    }

    @Test(expected = CreateSessionException.class)
    public void Should_ThrowException_When_SearchResultIsEmptyList() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        //mock for private method
        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).out(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).out(query, sessionIdIObject);
        IField collectionNameF = mock(IField.class);
        IField pageSizeF = mock(IField.class);
        IField pageNumberF = mock(IField.class);
        IField criteriaF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("collectionName"))).thenReturn(collectionNameF);
        when(IOC.resolve(eq(fieldKey), eq("pageSize"))).thenReturn(pageSizeF);
        when(IOC.resolve(eq(fieldKey), eq("pageNumber"))).thenReturn(pageNumberF);
        when(IOC.resolve(eq(fieldKey), eq("criteria"))).thenReturn(criteriaF);

        IField countSearchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("countSearchResult"))).thenReturn(countSearchResultF);
        when(countSearchResultF.in(searchQuery)).thenReturn(1);

        IField searchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("searchResult"))).thenReturn(searchResultF);
        when(searchResultF.in(searchQuery)).thenReturn(Collections.emptyList());

        actor.resolveSession(inputMessage);

        verify(searchTask).execute();
        verify(collectionNameF).out(searchQuery, collectionName);
        verify(pageSizeF).out(searchQuery, 1);
        verify(pageNumberF).out(searchQuery, 1);
    }

    @Test(expected = CreateSessionException.class)
    public void Should_searchSessionInDB_When_FoundSessionIsNull() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IDatabaseTask searchTask = mock(IDatabaseTask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq("PSQL"))).thenReturn(searchTask);

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);

        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).out(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).out(query, sessionIdIObject);
        IField collectionNameF = mock(IField.class);
        IField pageSizeF = mock(IField.class);
        IField pageNumberF = mock(IField.class);
        IField criteriaF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("collectionName"))).thenReturn(collectionNameF);
        when(IOC.resolve(eq(fieldKey), eq("pageSize"))).thenReturn(pageSizeF);
        when(IOC.resolve(eq(fieldKey), eq("pageNumber"))).thenReturn(pageNumberF);
        when(IOC.resolve(eq(fieldKey), eq("criteria"))).thenReturn(criteriaF);

        IField countSearchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("countSearchResult"))).thenReturn(countSearchResultF);
        when(countSearchResultF.in(searchQuery)).thenReturn(1);

        IField searchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("searchResult"))).thenReturn(searchResultF);
        IObject result = mock(IObject.class);
        when(searchResultF.in(searchQuery)).thenReturn(Collections.singletonList(result));

        IField sessionF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("session"))).thenReturn(sessionF);
        when(sessionF.in(result)).thenReturn(null);

        actor.resolveSession(inputMessage);

        verify(searchTask).execute();
        verify(collectionNameF).out(searchQuery, collectionName);
        verify(pageSizeF).out(searchQuery, 1);
        verify(pageNumberF).out(searchQuery, 1);
    }

    @Test
    public void Should_searchSessionInDB_When_SessionIdIsNotNullAndNotEmptyString() throws Exception {
        when(inputMessage.getSessionId()).thenReturn("123");

        IObject searchQuery = mock(IObject.class);
        when(IOC.resolve(eq(iObjectKey))).thenReturn(searchQuery);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(eq(connectionPool)).thenReturn(poolGuard);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(IOC.resolve(eq(storageConnectionKey), anyObject())).thenReturn(connection);
        when(poolGuard.getObject()).thenReturn(connection);

        ITask searchTask = mock(ITask.class);
        when(IOC.resolve(eq(iDatabaseTaskKey), eq(connection), anyString(), eq(searchQuery), any(IAction.class))).thenReturn(searchTask);
        LinkedList<IObject> items = mock(LinkedList.class);
        whenNew(LinkedList.class).withNoArguments().thenReturn(items);
        IObject sessionFromDB = mock(IObject.class);
        doAnswer((Answer<Void>) invocation -> {
            when(items.get(0)).thenReturn(sessionFromDB);
            when(items.isEmpty()).thenReturn(false);
            return null;
        }).when(searchTask).execute();

        IObject query = mock(IObject.class);
        IObject sessionIdIObject = mock(IObject.class);
        Mockito.doNothing().when(EQUALS_F).out(sessionIdIObject, "123");
        Mockito.doNothing().when(SESSION_ID_F).out(query, sessionIdIObject);


        IField countSearchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("countSearchResult"))).thenReturn(countSearchResultF);
        when(countSearchResultF.in(searchQuery)).thenReturn(1);

        IField searchResultF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("searchResult"))).thenReturn(searchResultF);
        IObject result = mock(IObject.class);
        when(searchResultF.in(searchQuery)).thenReturn(Collections.singletonList(result));

        IField sessionF = mock(IField.class);
        when(IOC.resolve(eq(fieldKey), eq("session"))).thenReturn(sessionF);

        actor.resolveSession(inputMessage);

        verify(inputMessage).setSession(eq(sessionFromDB));
        verify(searchTask).execute();
//        verify(collectionNameF).out(searchQuery, collectionName);
//        verify(sizeF).out(searchQuery, 1);
//        verify(numberF).out(searchQuery, 1);
    }
}