package info.smart_tools.smartactors.actor.change_password;

import info.smart_tools.smartactors.actor.change_password.exception.ChangePasswordException;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordMessage;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.security.encoding.encoders.IPasswordEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({ IOC.class, Keys.class, ChangePasswordActor.class})
@RunWith(PowerMockRunner.class)
public class ChangePasswordActorTest {
    private ChangePasswordActor testActor;
    private IPool connectionPool;

    private IPasswordEncoder passwordEncoder;

    private IField userIdField;
    private IField passwordField;
    private IField eqField;

    private static final String collectionName = "testCollection";
    private static final String testUserId = "testUserId";
    private static final String testPassword = "testPassword";

    private static boolean firstLaunch = true;

    @Before
    public void before() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        IField collectionNameField = mock(IField.class);
        IField pageSizeField = mock(IField.class);
        IField pageNumberField = mock(IField.class);
        IField pageField = mock(IField.class);
        IField filterField = mock(IField.class);

        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);
        when(IOC.resolve(iFieldKey, "size")).thenReturn(pageSizeField);
        when(IOC.resolve(iFieldKey, "number")).thenReturn(pageNumberField);
        when(IOC.resolve(iFieldKey, "page")).thenReturn(pageField);
        when(IOC.resolve(iFieldKey, "filter")).thenReturn(filterField);

        connectionPool = mock(IPool.class);

        passwordEncoder = mock(IPasswordEncoder.class);

        final String algorithmName = "testAlgorithm";
        final String charsetName = "testCharset";
        final String encoderName = "testEncoder";

        ChangePasswordConfig params = mock(ChangePasswordConfig.class);
        when(params.getCollectionName()).thenReturn(collectionName);
        when(params.getConnectionPool()).thenReturn(connectionPool);
        when(params.getAlgorithm()).thenReturn(algorithmName);
        when(params.getCharset()).thenReturn(charsetName);
        when(params.getEncoder()).thenReturn(encoderName);

        IKey passwordEncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("PasswordEncoder")).thenReturn(passwordEncoderKey);

        when(IOC.resolve(passwordEncoderKey, algorithmName, encoderName, charsetName)).thenReturn(passwordEncoder);

        userIdField = mock(IField.class);
        passwordField = mock(IField.class);
        eqField = mock(IField.class);

        when(IOC.resolve(iFieldKey, "userId")).thenReturn(userIdField);
        when(IOC.resolve(iFieldKey, "password")).thenReturn(passwordField);
        when(IOC.resolve(iFieldKey, "$eq")).thenReturn(eqField);

        testActor = new ChangePasswordActor(params);

        if (firstLaunch) {
            firstLaunch = false;
            verifyStatic(Mockito.times(8));
            Keys.getOrAdd(IField.class.getCanonicalName());

            verifyStatic();
            IOC.resolve(iFieldKey, "userId");
            verifyStatic();
            IOC.resolve(iFieldKey, "password");
            verifyStatic();
            IOC.resolve(iFieldKey, "$eq");
        }

        //checkParams
        verify(params/*, times(2)*/).getCollectionName();
        verify(params/*, times(2)*/).getAlgorithm();

        //saveVariables
        verify(params).getConnectionPool();

        //getting encoder
        verifyStatic();
        Keys.getOrAdd("PasswordEncoder");

        verify(params).getEncoder();
        verify(params).getCharset();

        verifyStatic();
        IOC.resolve(passwordEncoderKey, algorithmName, encoderName, charsetName);
    }

    @Test
    public void MustCorrectChangePassword() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        IObject searchQuery = mock(IObject.class);
        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doReturn(searchQuery).when(changePasswordActorSpy, "prepareQueryParams", message);

        LinkedList<IObject> items = mock(LinkedList.class);
        whenNew(LinkedList.class).withNoArguments().thenReturn(items);

        ITask searchTask = mock(ITask.class);

        IKey searchTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.search")).thenReturn(searchTaskKey);

        ArgumentCaptor<IAction> searchActionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);

        when(IOC.resolve
                        (eq(searchTaskKey),
                        eq(poolGuardObject),
                        eq(collectionName),
                        eq(searchQuery),
                        any(IAction.class))
        ).thenReturn(searchTask);

        doNothing().when(changePasswordActorSpy, "validateSearchResult", items, message);

        IObject user = mock(IObject.class);
        when(items.get(0)).thenReturn(user);

        String encodedPassword = "encP";
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);

        IKey upsertTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.upsert")).thenReturn(upsertTaskKey);

        ITask upsertTask = mock(ITask.class);
        when(IOC.resolve(
                upsertTaskKey,
                poolGuardObject,
                collectionName,
                user
        )).thenReturn(upsertTask);

        changePasswordActorSpy.changePassword(message);

        verifyNew(PoolGuard.class).withArguments(connectionPool);

        verify(message).getUserId();
        verify(message, times(2)).getPassword();

        verifyNew(LinkedList.class).withNoArguments();

        verifyStatic();
        Keys.getOrAdd("db.collection.search");

        verifyStatic();
        IOC.resolve
                (eq(searchTaskKey),
                        eq(poolGuardObject),
                        eq(collectionName),
                        eq(searchQuery),
                        searchActionArgumentCaptor.capture());

        verify(searchTask).execute();

        verify(items).get(0);

        verify(passwordField).out(user, encodedPassword);

        verifyStatic();
        Keys.getOrAdd("db.collection.upsert");

        verifyStatic();
        IOC.resolve(
                upsertTaskKey,
                poolGuardObject,
                collectionName,
                user
        );

        verify(upsertTask).execute();
        verify(poolGuard).close();
    }

    @Test
    public void MustInCorrectChangePasswordWhenCantCreateNewPoolGuard() throws Exception {
        whenNew(PoolGuard.class).withArguments(connectionPool).thenThrow(new InvalidArgumentException(""));

        try {
            testActor.changePassword(null);
        } catch (ChangePasswordException e) {
            verifyNew(PoolGuard.class).withArguments(connectionPool);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenGetUserIdThrowException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenThrow(new ReadValueException());

        try {
            testActor.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenGetUserIdIsNull() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(null);

        try {
            testActor.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenGetPasswordIsNull() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(null);

        try {
            testActor.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();
            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenPrepareQueryParamsThrowReadValueException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doThrow(new ReadValueException()).when(changePasswordActorSpy, "prepareQueryParams", message);

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenPrepareQueryParamsThrowInvalidArgumentException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doThrow(new InvalidArgumentException("")).when(changePasswordActorSpy, "prepareQueryParams", message);

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenPrepareQueryParamsThrowChangeValueException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doThrow(new ChangeValueException()).when(changePasswordActorSpy, "prepareQueryParams", message);

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenPrepareQueryParamsThrowResolutionException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doThrow(new ResolutionException("")).when(changePasswordActorSpy, "prepareQueryParams", message);

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenIOCResolveThrowException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        IObject searchQuery = mock(IObject.class);
        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doReturn(searchQuery).when(changePasswordActorSpy, "prepareQueryParams", message);

        LinkedList<IObject> items = mock(LinkedList.class);
        whenNew(LinkedList.class).withNoArguments().thenReturn(items);

        IKey searchTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.search")).thenReturn(searchTaskKey);

        when(IOC.resolve
                (eq(searchTaskKey),
                        eq(poolGuardObject),
                        eq(collectionName),
                        eq(searchQuery),
                        any(IAction.class))
        ).thenThrow(new ResolutionException(""));

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verifyNew(LinkedList.class).withNoArguments();

            verifyStatic();
            Keys.getOrAdd("db.collection.search");

            verifyStatic();
            IOC.resolve
                    (eq(searchTaskKey),
                            eq(poolGuardObject),
                            eq(collectionName),
                            eq(searchQuery),
                            any(IAction.class));

            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenFieldOutThrowChangeValueException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        IObject searchQuery = mock(IObject.class);
        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doReturn(searchQuery).when(changePasswordActorSpy, "prepareQueryParams", message);

        LinkedList<IObject> items = mock(LinkedList.class);
        whenNew(LinkedList.class).withNoArguments().thenReturn(items);

        ITask searchTask = mock(ITask.class);

        IKey searchTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.search")).thenReturn(searchTaskKey);

        ArgumentCaptor<IAction> searchActionArgumentCaptor = ArgumentCaptor.forClass(IAction.class);

        when(IOC.resolve
                (eq(searchTaskKey),
                        eq(poolGuardObject),
                        eq(collectionName),
                        eq(searchQuery),
                        any(IAction.class))
        ).thenReturn(searchTask);

        doNothing().when(changePasswordActorSpy, "validateSearchResult", items, message);

        IObject user = mock(IObject.class);
        when(items.get(0)).thenReturn(user);

        String encodedPassword = "encP";
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);

        doThrow(new ChangeValueException()).when(passwordField).out(user, encodedPassword);

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message, times(2)).getPassword();

            verifyNew(LinkedList.class).withNoArguments();

            verifyStatic();
            Keys.getOrAdd("db.collection.search");

            verifyStatic();
            IOC.resolve
                    (eq(searchTaskKey),
                            eq(poolGuardObject),
                            eq(collectionName),
                            eq(searchQuery),
                            searchActionArgumentCaptor.capture());

            verify(searchTask).execute();

            verify(items).get(0);

            verify(passwordField).out(user, encodedPassword);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectChangePasswordWhenTaskExecuteThrowException() throws Exception {
        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        Object poolGuardObject = mock(Object.class);
        when(poolGuard.getObject()).thenReturn(poolGuardObject);

        ChangePasswordMessage message = mock(ChangePasswordMessage.class);
        when(message.getUserId()).thenReturn(testUserId);
        when(message.getPassword()).thenReturn(testPassword);

        IObject searchQuery = mock(IObject.class);
        ChangePasswordActor changePasswordActorSpy = spy(testActor);

        doReturn(searchQuery).when(changePasswordActorSpy, "prepareQueryParams", message);

        LinkedList<IObject> items = mock(LinkedList.class);
        whenNew(LinkedList.class).withNoArguments().thenReturn(items);

        ITask searchTask = mock(ITask.class);

        IKey searchTaskKey = mock(IKey.class);
        when(Keys.getOrAdd("db.collection.search")).thenReturn(searchTaskKey);

        when(IOC.resolve
                (eq(searchTaskKey),
                        eq(poolGuardObject),
                        eq(collectionName),
                        eq(searchQuery),
                        any(IAction.class))
        ).thenReturn(searchTask);

        doThrow(new TaskExecutionException("")).when(searchTask).execute();

        try {
            changePasswordActorSpy.changePassword(message);
        } catch (ChangePasswordException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verify(message).getUserId();
            verify(message).getPassword();

            verifyNew(LinkedList.class).withNoArguments();

            verifyStatic();
            Keys.getOrAdd("db.collection.search");

            verifyStatic();
            IOC.resolve
                    (eq(searchTaskKey),
                            eq(poolGuardObject),
                            eq(collectionName),
                            eq(searchQuery),
                            any(IAction.class));

            verify(searchTask).execute();
            return;
        }
        assertTrue(false);
    }
}