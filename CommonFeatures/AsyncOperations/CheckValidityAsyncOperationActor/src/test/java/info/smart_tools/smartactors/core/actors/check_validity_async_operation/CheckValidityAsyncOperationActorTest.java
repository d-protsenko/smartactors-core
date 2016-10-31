package info.smart_tools.smartactors.core.actors.check_validity_async_operation;

import info.smart_tools.smartactors.core.actors.check_validity_async_operation.exception.InvalidAsyncOperationIdException;
import info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper.CheckValidityMessage;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Arrays;

public class CheckValidityAsyncOperationActorTest {
    private CheckValidityAsyncOperationActor actor;
    private CheckValidityMessage message;

    @org.junit.Before
    public void setUp() throws Exception {
        message = PowerMockito.mock(CheckValidityMessage.class);

        actor = new CheckValidityAsyncOperationActor(PowerMockito.mock(IObject.class));
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_InvalidId() throws ReadValueException, ChangeValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(message.getIdentifiers()).thenReturn(Arrays.asList("111", "222", "333"));
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_ListOfIdsIsNull() throws ChangeValueException, ReadValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(message.getIdentifiers()).thenReturn(null);
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }

    @Test(expected = InvalidAsyncOperationIdException.class)
    public void Should_ThrowException_When_ListOfIdsIsEmpty() throws ChangeValueException, ReadValueException, InvalidAsyncOperationIdException {
        PowerMockito.when(message.getIdentifiers()).thenReturn(new ArrayList<>());
        PowerMockito.when(message.getAsyncOperationId()).thenReturn("000");
        actor.check(message);
    }
}