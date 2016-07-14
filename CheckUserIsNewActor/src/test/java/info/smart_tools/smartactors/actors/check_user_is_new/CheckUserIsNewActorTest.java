package info.smart_tools.smartactors.actors.check_user_is_new;

import info.smart_tools.smartactors.actors.check_user_is_new.wrapper.ActorParams;
import info.smart_tools.smartactors.actors.check_user_is_new.wrapper.MessageWrapper;
import info.smart_tools.smartactors.core.cached_collection.CachedCollection;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class CheckUserIsNewActorTest {
    CheckUserIsNewActor actor;
    CachedCollection collection = mock(CachedCollection.class);

    @Before
    public void setUp() throws Exception {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IKey collectionKey = mock(IKey.class);
        when(Keys.getOrAdd(CachedCollection.class.toString())).thenReturn(collectionKey);
        when(IOC.resolve(collectionKey, "user")).thenReturn(collection);

        ActorParams params = mock(ActorParams.class);
        when(params.getCollectionName()).thenReturn("user");
        actor = new CheckUserIsNewActor(params);
    }

    @Test
    public void shouldNotThrowExceptions() throws Exception {
        MessageWrapper message = mock(MessageWrapper.class);
        when(message.getEmail()).thenReturn("email");
        when(collection.getItems("email")).thenReturn(Collections.EMPTY_LIST);
        actor.check(message);
    }

    @Test(expected = TaskExecutionException.class)
    public void shouldThrowExceptionWhenUsersListIsNotEmpty() throws Exception {
        IObject user1 = mock(IObject.class);
        IObject user2 = mock(IObject.class);
        MessageWrapper message = mock(MessageWrapper.class);
        when(message.getEmail()).thenReturn("email");
        when(collection.getItems("email")).thenReturn(Arrays.asList(user1, user2));
        actor.check(message);
    }
}
