package info.smart_tools.smartactors.actors.authentication;

import info.smart_tools.smartactors.actors.authentication.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.authentication.wrapper.AuthenticationMessage;
import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(IOC.class)
@RunWith(PowerMockRunner.class)
public class AuthenticationActorTest {
    AuthenticationActor actor;

    @Before
    public void setUp() throws Exception {
        ActorParams params = mock(ActorParams.class);
        when(params.getUserAgentFieldName()).thenReturn("123");
        actor = new AuthenticationActor(params);
    }

    @Test
    public void shouldValidateCorrectly() throws Exception {
        AuthenticationMessage message = mock(AuthenticationMessage.class);
        IObject authInfo = mock(IObject.class);
        when(message.getAuthInfo()).thenReturn(authInfo);
        when(authInfo.getValue(eq(new FieldName("123")))).thenReturn("authInfo");

        actor.authenticateSession(message);
    }

    @Test
    public void shouldSetErrorToMessage() throws Exception {
        AuthenticationMessage message = mock(AuthenticationMessage.class);
        IObject authInfo = mock(IObject.class);
        when(message.getAuthInfo()).thenReturn(authInfo);
        when(authInfo.getValue(eq(new FieldName("123")))).thenReturn(null);

        actor.authenticateSession(message);
        verify(message).setError("Created session is broken");
    }

}
