package info.smart_tools.smartactors.actor.change_password;

import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordConfig;
import info.smart_tools.smartactors.actor.change_password.wrapper.ChangePasswordMessage;
import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.itask.ITask;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.IPoolGuard;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.security.encoding.encoders.IPasswordEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({ IOC.class, Keys.class, ChangePasswordActor.class})
@RunWith(PowerMockRunner.class)
public class ChangePasswordActorTest {
    private ChangePasswordActor testActor;
    private IPool connectionPool;

    private IPasswordEncoder passwordEncoder;

    private static IField userIdField;
    private static IField passwordField;
    private static IField eqField;

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

        if (firstLaunch) {

            userIdField = mock(IField.class);
            passwordField = mock(IField.class);
            eqField = mock(IField.class);

            when(IOC.resolve(iFieldKey, "userId")).thenReturn(userIdField);
            when(IOC.resolve(iFieldKey, "password")).thenReturn(passwordField);
            when(IOC.resolve(iFieldKey, "$eq")).thenReturn(eqField);
        }

        testActor = new ChangePasswordActor(params);

        if (firstLaunch) {
            firstLaunch = false;
            verifyStatic(Mockito.times(3));
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
    }
}