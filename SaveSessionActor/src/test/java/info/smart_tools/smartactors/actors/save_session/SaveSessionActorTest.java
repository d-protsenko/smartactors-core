package info.smart_tools.smartactors.actors.save_session;

import info.smart_tools.smartactors.actors.save_session.exception.SaveSessionException;
import info.smart_tools.smartactors.actors.save_session.wrapper.SaveSessionMessage;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.database.interfaces.istorage_connection.IStorageConnection;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.wrapper.ConnectionOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class, SaveSessionActor.class})
public class SaveSessionActorTest {
    private IPool connectionPool;
    private String collectionName;

    private SaveSessionActor testActor;

    @Before
    public void before() throws ResolutionException, SaveSessionException, ReadValueException, InvalidArgumentException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        collectionName = "exampleCN";
        connectionPool = mock(IPool.class);

        IObject params = mock(IObject.class);

        IField collectionNameField = mock(IField.class);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);
        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);

        when(collectionNameField.in(params)).thenReturn(collectionName);

        ConnectionOptions options = mock(ConnectionOptions.class);

        IKey postgresOptionsKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionOptions")).thenReturn(postgresOptionsKey);
        when(IOC.resolve(postgresOptionsKey)).thenReturn(options);

        IKey postgresPoolKey = mock(IKey.class);
        when(Keys.getOrAdd("PostgresConnectionPool")).thenReturn(postgresPoolKey);
        when(IOC.resolve(postgresPoolKey, options)).thenReturn(connectionPool);

        testActor = new SaveSessionActor(params);

        verifyStatic();
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(iFieldKey, "collectionName");

        verify(collectionNameField).in(params);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionOptions");

        verifyStatic();
        IOC.resolve(postgresOptionsKey);

        verifyStatic();
        Keys.getOrAdd("PostgresConnectionPool");

        verifyStatic();
        IOC.resolve(postgresPoolKey, options);
    }

    @Test
    public void MustCorrectSaveSession() throws Exception {
        SaveSessionMessage message = mock(SaveSessionMessage.class);
        IObject session = mock(IObject.class);
        when(message.getSession()).thenReturn(session);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(poolGuard.getObject()).thenReturn(connection);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.upsert")).thenReturn(upsertTaskKey);

        ITask task = mock(ITask.class);
        when(IOC.resolve(upsertTaskKey, connection, collectionName, session)).thenReturn(task);

        testActor.saveSession(message);

        verifyNew(PoolGuard.class).withArguments(connectionPool);

        verify(poolGuard).getObject();

        verifyStatic();
        Keys.getOrAdd("db.collection.upsert");

        verify(message).getSession();

        verifyStatic();
        IOC.resolve(upsertTaskKey, connection, collectionName, session);

        verify(task).execute();

        verify(poolGuard).close();
    }

    @Test
    public void MustInCorrectSaveSessionWhenCantCreatePoolGuard() throws Exception {
        whenNew(PoolGuard.class).withArguments(connectionPool).thenThrow(new InvalidArgumentException(""));

        try {
            testActor.saveSession(null);
        } catch (SaveSessionException e) {
            verifyNew(PoolGuard.class).withArguments(connectionPool);
            return;
        }
        fail();
    }

    @Test
    public void MustInCorrectSaveSessionWhenKeysGetOrAddThrowException() throws Exception {
        SaveSessionMessage message = mock(SaveSessionMessage.class);
        IObject session = mock(IObject.class);
        when(message.getSession()).thenReturn(session);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(poolGuard.getObject()).thenReturn(connection);

        when(Keys.getOrAdd("db.collection.upsert")).thenThrow(new ResolutionException(""));

        try {
            testActor.saveSession(message);
        } catch (SaveSessionException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(poolGuard).getObject();

            verifyStatic();
            Keys.getOrAdd("db.collection.upsert");

            verify(poolGuard).close();
            return;
        }
        fail();
    }

    @Test
    public void MustInCorrectSaveSessionWhenIOCResolveThrowException() throws Exception {
        SaveSessionMessage message = mock(SaveSessionMessage.class);
        IObject session = mock(IObject.class);
        when(message.getSession()).thenReturn(session);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(poolGuard.getObject()).thenReturn(connection);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.upsert")).thenReturn(upsertTaskKey);

        when(IOC.resolve(upsertTaskKey, connection, collectionName, session)).thenThrow(new ResolutionException(""));

        try {
            testActor.saveSession(message);
        } catch (SaveSessionException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(poolGuard).getObject();

            verifyStatic();
            Keys.getOrAdd("db.collection.upsert");

            verify(message).getSession();

            verifyStatic();
            IOC.resolve(upsertTaskKey, connection, collectionName, session);

            verify(poolGuard).close();
            return;
        }
        fail();
    }

    @Test
    public void MustInCorrectSaveSessionWhenTaskExecuteThrowException() throws Exception {
        SaveSessionMessage message = mock(SaveSessionMessage.class);
        IObject session = mock(IObject.class);
        when(message.getSession()).thenReturn(session);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IStorageConnection connection = mock(IStorageConnection.class);
        when(poolGuard.getObject()).thenReturn(connection);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.upsert")).thenReturn(upsertTaskKey);

        ITask task = mock(ITask.class);
        when(IOC.resolve(upsertTaskKey, connection, collectionName, session)).thenReturn(task);

        doThrow(new TaskExecutionException("")).when(task).execute();

        try {
            testActor.saveSession(message);
        } catch (SaveSessionException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(poolGuard).getObject();

            verifyStatic();
            Keys.getOrAdd("db.collection.upsert");

            verify(message).getSession();

            verifyStatic();
            IOC.resolve(upsertTaskKey, connection, collectionName, session);

            verify(task).execute();

            verify(poolGuard).close();
            return;
        }
        fail();
    }
}