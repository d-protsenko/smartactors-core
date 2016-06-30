package info.smart_tools.smartactors.core.actors.check_validity_async_operation;

import info.smart_tools.smartactors.core.actors.check_validity_async_operation.exception.InvalidAsyncOperationIdException;
import info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper.CheckValidityMessage;
import info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper.Session;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CheckValidityAsyncOperationActorTest {
    private CheckValidityAsyncOperationActor actor;
    private Session session;
    private CheckValidityMessage message;

    @org.junit.Before
    public void setUp() throws Exception {
        session = PowerMockito.mock(Session.class);
        message = PowerMockito.mock(CheckValidityMessage.class);
        PowerMockito.when(message.getSession()).thenReturn(session);

        actor = new CheckValidityAsyncOperationActor(PowerMockito.mock(IObject.class));
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_InvalidId() throws ReadValueException, ChangeValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(session.getIdentifiers()).thenReturn(Arrays.asList("111", "222", "333"));
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_ListOfIdsIsNull() throws ChangeValueException, ReadValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(session.getIdentifiers()).thenReturn(null);
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_ListOfIdsIsEmpty() throws ChangeValueException, ReadValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(session.getIdentifiers()).thenReturn(new ArrayList<>());
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }
}