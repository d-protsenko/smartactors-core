package info.smart_tools.smartactors.actors.create_session.actors;

import info.smart_tools.smartactors.core.ipool.IPool;
import info.smart_tools.smartactors.actors.create_session.CreateSessionActor;
import info.smart_tools.smartactors.actors.create_session.exception.CreateSessionException;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionConfig;
import info.smart_tools.smartactors.actors.create_session.wrapper.CreateSessionMessage;
import info.smart_tools.smartactors.actors.create_session.wrapper.Session;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

    @org.junit.Before
    public void setUp() throws Exception {
        inputMessage = mock(CreateSessionMessage.class);
        session = mock(Session.class);
        authInfo = mock(IObject.class);
        when(inputMessage.getAuthInfo()).thenReturn(authInfo);

        mockStatic(IOC.class);
        IKey key = mock(IKey.class);
        IKey sessionKey = mock(IKey.class);
        when(IOC.getKeyForKeyStorage()).thenReturn(key);
        when(IOC.resolve(eq(key), eq("interface info.smart_tools.smartactors.actors.create_session.wrapper.Session"))).thenReturn(sessionKey);
        when(IOC.resolve(eq(sessionKey))).thenReturn(session);


        CreateSessionConfig config = mock(CreateSessionConfig.class);
        when(config.getCollectionName()).thenReturn("collectionName");
        IPool connectionPool = mock(IPool.class);
        when(config.getConnectionPool()).thenReturn(connectionPool);
        actor = new CreateSessionActor(config);
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdIsNull() throws ChangeValueException, ReadValueException, CreateSessionException {
        when(inputMessage.getSessionId()).thenReturn(null);
        actor.createSession(inputMessage);
        verify(session).setAuthInfo(eq(authInfo));
        verify(inputMessage).setSession(eq(session));
    }

    @Test
    public void Should_insertNewSessionInMessage_When_SessionIdEqualEmptyString() throws CreateSessionException, ReadValueException, ChangeValueException {
        when(inputMessage.getSessionId()).thenReturn("");
        actor.createSession(inputMessage);
        verify(session).setAuthInfo(eq(authInfo));
        verify(inputMessage).setSession(eq(session));
    }

    @Test
    public void Should_searchSessionInDB_When_SessionIdIsNotNullAndNotEmptyString() {
        //TODO:: add test
    }
}