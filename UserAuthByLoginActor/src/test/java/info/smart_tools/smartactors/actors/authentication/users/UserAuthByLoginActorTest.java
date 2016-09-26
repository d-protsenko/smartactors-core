package info.smart_tools.smartactors.actors.authentication.users;


import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMessage;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.base.interfaces.ipool.IPool;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.base.pool_guard.PoolGuard;
import info.smart_tools.smartactors.base.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.security.encoding.encoders.IPasswordEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({ IOC.class, Keys.class, UserAuthByLoginActor.class})
@RunWith(PowerMockRunner.class)
public class UserAuthByLoginActorTest {
    private UserAuthByLoginActor authByLoginActor;
    private IPool connectionPool;
    private IPasswordEncoder passwordEncoder;

    private IField loginField;
    private IField passwordField;
    private IField eqField;

    private IField collectionNameField;
    private IField pageSizeField;
    private IField pageNumberField;
    private IField pageField;
    private IField filterField;

    private IKey iFieldKey;

    private String collectionName = "testCollection";
    private String testLogin = "testLogin";

    @Before
    public void setUp() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        connectionPool = mock(IPool.class);

        passwordEncoder = mock(IPasswordEncoder.class);

        final String algorithmName = "testAlgorithm";
        final String charsetName = "testCharset";
        final String encoderName = "testEncoder";

        IUserAuthByLoginParams params = mock(IUserAuthByLoginParams.class);
        when(params.getCollection()).thenReturn(collectionName);
        when(params.getConnectionPool()).thenReturn(connectionPool);
        when(params.getAlgorithm()).thenReturn(algorithmName);
        when(params.getCharset()).thenReturn(charsetName);
        when(params.getEncoder()).thenReturn(encoderName);

        IKey passwordEncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("PasswordEncoder")).thenReturn(passwordEncoderKey);

        when(IOC.resolve(passwordEncoderKey, algorithmName, encoderName, charsetName)).thenReturn(passwordEncoder);


        loginField = mock(IField.class);
        passwordField = mock(IField.class);
        eqField = mock(IField.class);

        collectionNameField= mock(IField.class);
        pageSizeField = mock(IField.class);
        pageNumberField = mock(IField.class);
        pageField = mock(IField.class);
        filterField = mock(IField.class);

        when(IOC.resolve(iFieldKey, "collectionName")).thenReturn(collectionNameField);
        when(IOC.resolve(iFieldKey, "size")).thenReturn(pageSizeField);
        when(IOC.resolve(iFieldKey, "number")).thenReturn(pageNumberField);
        when(IOC.resolve(iFieldKey, "page")).thenReturn(pageField);
        when(IOC.resolve(iFieldKey, "filter")).thenReturn(filterField);

        when(IOC.resolve(iFieldKey, "email")).thenReturn(loginField);
        when(IOC.resolve(iFieldKey, "password")).thenReturn(passwordField);
        when(IOC.resolve(iFieldKey, "$eq")).thenReturn(eqField);

        authByLoginActor = new UserAuthByLoginActor(params);

        //checkParams
        verify(params, times(2)).getCollection();
        verify(params, times(2)).getAlgorithm();

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
    public void MustCorrectAuthenticateUser() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doNothing().when(authByLoginActorSpy, "checkMsg", message);

        IObject user = mock(IObject.class);

        doReturn(user).when(authByLoginActorSpy, "resolveLogin", message, poolGuard);

        doNothing().when(authByLoginActorSpy, "validatePassword", message, user);
        doNothing().when(authByLoginActorSpy, "setSuccessResponse", message);

        authByLoginActorSpy.authenticateUser(message);

        verifyNew(PoolGuard.class).withArguments(connectionPool);

        verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);
        verifyPrivate(authByLoginActorSpy).invoke("resolveLogin", message, poolGuard);
        verifyPrivate(authByLoginActorSpy).invoke("validatePassword", message, user);
        verifyPrivate(authByLoginActorSpy).invoke("setSuccessResponse", message);

        verify(poolGuard).close();
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenCreatingPoolGuardThrowException() throws Exception {

        whenNew(PoolGuard.class).withArguments(connectionPool).thenThrow(new PoolGuardException(""));

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        try {
            authByLoginActor.authenticateUser(message);
        } catch (AuthenticateUserException e) {
            verifyNew(PoolGuard.class).withArguments(connectionPool);
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenCheckMsgThrowInvalidArgumentException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doThrow(new InvalidArgumentException("")).when(authByLoginActorSpy, "checkMsg", message);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenCheckMsgThrowReadValueException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doThrow(new ReadValueException()).when(authByLoginActorSpy, "checkMsg", message);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenCheckMsgThrowChangeValueException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doThrow(new ChangeValueException()).when(authByLoginActorSpy, "checkMsg", message);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenResolveLoginThrowException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doNothing().when(authByLoginActorSpy, "checkMsg", message);

        doThrow(new AuthenticateUserException("")).when(authByLoginActorSpy, "resolveLogin", message, poolGuard);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {
            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);
            verifyPrivate(authByLoginActorSpy).invoke("resolveLogin", message, poolGuard);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }



    @Test
    public void MustInCorrectAuthenticateUserWhenValidatePasswordThrowException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doNothing().when(authByLoginActorSpy, "checkMsg", message);

        IObject user = mock(IObject.class);

        doReturn(user).when(authByLoginActorSpy, "resolveLogin", message, poolGuard);

        doThrow(new AuthenticateUserException("")).when(authByLoginActorSpy, "validatePassword", message, user);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {
            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);
            verifyPrivate(authByLoginActorSpy).invoke("resolveLogin", message, poolGuard);
            verifyPrivate(authByLoginActorSpy).invoke("validatePassword", message, user);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustInCorrectAuthenticateUserWhenSetSuccessResponseThrowException() throws Exception {
        UserAuthByLoginActor authByLoginActorSpy;
        authByLoginActorSpy = spy(authByLoginActor);

        PoolGuard poolGuard = mock(PoolGuard.class);
        whenNew(PoolGuard.class).withArguments(connectionPool).thenReturn(poolGuard);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        doNothing().when(authByLoginActorSpy, "checkMsg", message);

        IObject user = mock(IObject.class);

        doReturn(user).when(authByLoginActorSpy, "resolveLogin", message, poolGuard);

        doNothing().when(authByLoginActorSpy, "validatePassword", message, user);
        doThrow(new ChangeValueException()).when(authByLoginActorSpy, "setSuccessResponse", message);

        try {
            authByLoginActorSpy.authenticateUser(message);
        } catch (AuthenticateUserException e) {

            verifyNew(PoolGuard.class).withArguments(connectionPool);

            verifyPrivate(authByLoginActorSpy).invoke("checkMsg", message);
            verifyPrivate(authByLoginActorSpy).invoke("resolveLogin", message, poolGuard);
            verifyPrivate(authByLoginActorSpy).invoke("validatePassword", message, user);
            verifyPrivate(authByLoginActorSpy).invoke("setSuccessResponse", message);

            verify(poolGuard).close();
            return;
        }
        assertTrue(false);
    }

    @Test
    public void MustCorrectPrepareQueryParams() throws Exception {
        Method method = authByLoginActor.getClass().getDeclaredMethod("prepareQueryParams", IUserAuthByLoginMessage.class);
        method.setAccessible(true);

        IUserAuthByLoginMessage message = mock(IUserAuthByLoginMessage.class);

        when(message.getLogin()).thenReturn(testLogin);

        IKey iObjectKey = mock(IKey.class);
        IObject filter = mock(IObject.class);
        IObject page = mock(IObject.class);
        IObject searchQuery = mock(IObject.class);
        IObject loginObject = mock(IObject.class);

        when(Keys.getOrAdd(IObject.class.getCanonicalName())).thenReturn(iObjectKey);
        when(IOC.resolve(iObjectKey)).thenReturn(filter).thenReturn(page).thenReturn(searchQuery).thenReturn(loginObject);

        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        assertTrue(method.invoke(authByLoginActor, message) == searchQuery);
        method.setAccessible(false);

        verifyStatic(times(4));
        Keys.getOrAdd(IObject.class.getCanonicalName());

        verifyStatic(times(8));//plus static invocations
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(iFieldKey, "collectionName");

        verifyStatic();
        IOC.resolve(iFieldKey, "size");

        verifyStatic();
        IOC.resolve(iFieldKey, "number");

        verifyStatic();
        IOC.resolve(iFieldKey, "page");

        verifyStatic();
        IOC.resolve(iFieldKey, "filter");

        verify(collectionNameField).out(searchQuery, collectionName);
        verify(pageSizeField).out(page, 1);
        verify(pageNumberField).out(page, 1);
        verify(pageField).out(searchQuery, page);
        verify(filterField).out(searchQuery, filter);

        verify(message).getLogin();
        verify(eqField).out(loginObject, testLogin);
        verify(loginField).out(filter, loginObject);
    }
}
