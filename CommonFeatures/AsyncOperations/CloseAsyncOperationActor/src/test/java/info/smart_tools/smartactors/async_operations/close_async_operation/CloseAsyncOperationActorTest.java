package info.smart_tools.smartactors.async_operations.close_async_operation;

import info.smart_tools.smartactors.async_operations.close_async_operation.wrapper.CloseAsyncOpMessage;
import info.smart_tools.smartactors.database.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.database.async_operation_collection.exception.CompleteAsyncOperationException;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@PrepareForTest({IOC.class, Keys.class, CloseAsyncOperationActor.class})
@RunWith(PowerMockRunner.class)
public class CloseAsyncOperationActorTest {

    private CloseAsyncOperationActor testActor;
    private IAsyncOperationCollection targetCollection;

    @Before
    public void before() throws ResolutionException, ReadValueException, InvalidArgumentException {
        mockStatic(IOC.class);
        mockStatic(Keys.class);

        IObject actorParams = mock(IObject.class);
        String collectionName = "asdasd";

        IField collectionNameField = mock(IField.class);
        IKey collectionNameFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(collectionNameFieldKey);
        when(IOC.resolve(collectionNameFieldKey, "collectionName")).thenReturn(collectionNameField);

        when(collectionNameField.in(actorParams)).thenReturn(collectionName);

        IKey collectionKey = mock(IKey.class);
        when(Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName())).thenReturn(collectionKey);

        targetCollection = mock(IAsyncOperationCollection.class);
        when(IOC.resolve(collectionKey, collectionName)).thenReturn(targetCollection);

        testActor = new CloseAsyncOperationActor(actorParams);

        verifyStatic();
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(collectionNameFieldKey, "collectionName");

        verify(collectionNameField).in(actorParams);

        verifyStatic();
        Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName());

        verifyStatic();
        IOC.resolve(collectionKey, collectionName);
    }

    @Test
    public void MustCorrectCompleteAsyncOperation() throws ReadValueException, CompleteAsyncOperationException, InvalidArgumentException {
        String targetToken = "targetToken";

        List<String> allTokens = mock(List.class);

        IObject operation = mock(IObject.class);

        CloseAsyncOpMessage message = mock(CloseAsyncOpMessage.class);
        when(message.getToken()).thenReturn(targetToken);
        when(message.getOperationTokens()).thenReturn(allTokens);
        when(message.getOperation()).thenReturn(operation);

        testActor.completeAsyncOp(message);

        verify(message).getToken();
        verify(message).getOperationTokens();
        verify(allTokens).remove(targetToken);

        verify(message).getOperation();
        verify(targetCollection).complete(operation);
    }

    @Test
    public void MustInCorrectCompleteAsyncOperationWhenMessageThrowReadValueException() throws ReadValueException {

        CloseAsyncOpMessage message = mock(CloseAsyncOpMessage.class);
        when(message.getToken()).thenThrow(new ReadValueException());

        try {
            testActor.completeAsyncOp(message);
        } catch (InvalidArgumentException e) {
            verify(message).getToken();
            return;
        }
        assertTrue("Must throw exception", false);
    }

    @Test
    public void MustInCorrectCompleteAsyncOperationWhenCompleteCallInTargetCollectionThrowException() throws ReadValueException, CompleteAsyncOperationException {
        String targetToken = "targetToken";

        List<String> allTokens = mock(List.class);

        IObject operation = mock(IObject.class);

        CloseAsyncOpMessage message = mock(CloseAsyncOpMessage.class);
        when(message.getToken()).thenReturn(targetToken);
        when(message.getOperationTokens()).thenReturn(allTokens);
        when(message.getOperation()).thenReturn(operation);

        doThrow(new CompleteAsyncOperationException("")).when(targetCollection).complete(operation);

        try {
            testActor.completeAsyncOp(message);
        } catch (InvalidArgumentException e) {

            verify(message).getToken();
            verify(message).getOperationTokens();
            verify(allTokens).remove(targetToken);

            verify(message).getOperation();
            verify(targetCollection).complete(operation);
            return;
        }
        assertTrue("Must throw exception", false);
    }

}