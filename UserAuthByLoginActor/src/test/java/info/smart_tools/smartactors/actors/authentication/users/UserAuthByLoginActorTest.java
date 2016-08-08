package info.smart_tools.smartactors.actors.authentication.users;


import info.smart_tools.smartactors.actors.authentication.users.exceptions.AuthenticateUserException;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginMessage;
import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.pool_guard.PoolGuard;
import info.smart_tools.smartactors.core.pool_guard.exception.PoolGuardException;
import info.smart_tools.smartactors.core.security.encoding.encoders.IPasswordEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@PrepareForTest({ IOC.class, Keys.class, UserAuthByLoginActor.class})
@RunWith(PowerMockRunner.class)
public class UserAuthByLoginActorTest {
    private UserAuthByLoginActor authByLoginActor;
    private IPool connectionPool;
    private IPasswordEncoder passwordEncoder;

    private IField loginField;
    private IField passwordField;
    private IField eqField;

    private static final String COLLECTION_NAME = "testCollection";
    private static final String LOGIN = "testLogin";
    private static final String PASSWORD = "testPassword";

    private static boolean firstLaunch = true;

    @Before
    public void setUp() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);

        loginField = mock(IField.class);
        passwordField = mock(IField.class);
        eqField = mock(IField.class);

        connectionPool = mock(IPool.class);

        passwordEncoder = mock(IPasswordEncoder.class);

        when(IOC.resolve(iFieldKey, "email")).thenReturn(loginField);
        when(IOC.resolve(iFieldKey, "password")).thenReturn(passwordField);
        when(IOC.resolve(iFieldKey, "$eq")).thenReturn(eqField);

        final String algorithmName = "testAlgorithm";
        final String charsetName = "testCharset";
        final String encoderName = "testEncoder";

        IUserAuthByLoginParams params = mock(IUserAuthByLoginParams.class);
        when(params.getCollection()).thenReturn(COLLECTION_NAME);
        when(params.getConnectionPool()).thenReturn(connectionPool);
        when(params.getAlgorithm()).thenReturn(algorithmName);
        when(params.getCharset()).thenReturn(charsetName);
        when(params.getEncoder()).thenReturn(encoderName);

        IKey passwordEncoderKey = mock(IKey.class);
        when(Keys.getOrAdd("PasswordEncoder")).thenReturn(passwordEncoderKey);

        when(IOC.resolve(passwordEncoderKey, algorithmName, encoderName, charsetName)).thenReturn(passwordEncoder);

        authByLoginActor = new UserAuthByLoginActor(params);

        if (firstLaunch) {
            firstLaunch = false;
            verifyStatic(Mockito.times(3));
            Keys.getOrAdd(IField.class.getCanonicalName());

            verifyStatic();
            IOC.resolve(iFieldKey, "email");
            verifyStatic();
            IOC.resolve(iFieldKey, "password");
            verifyStatic();
            IOC.resolve(iFieldKey, "$eq");
        }

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
}
