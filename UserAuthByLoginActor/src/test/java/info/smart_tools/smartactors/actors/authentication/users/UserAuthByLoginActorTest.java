package info.smart_tools.smartactors.actors.authentication.users;

import info.smart_tools.smartactors.actors.authentication.users.wrappers.IUserAuthByLoginParams;
import org.junit.Before;

import static org.mockito.Mockito.mock;

public class UserAuthByLoginActorTest {
    private UserAuthByLoginActor authByLoginActor;

    @Before
    public void setUp() {
        IUserAuthByLoginParams params = mock(IUserAuthByLoginParams.class);

    }
}
