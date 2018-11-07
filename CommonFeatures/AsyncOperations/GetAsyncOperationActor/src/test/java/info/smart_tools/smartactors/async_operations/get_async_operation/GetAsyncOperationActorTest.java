package info.smart_tools.smartactors.async_operations.get_async_operation;

import info.smart_tools.smartactors.async_operations.get_async_operation.exception.GetAsyncOperationActorException;
import info.smart_tools.smartactors.async_operations.get_async_operation.wrapper.GetAsyncOperationMessage;
import info.smart_tools.smartactors.database.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.database.async_operation_collection.exception.GetAsyncOperationException;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_field_names_storage.Keys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IOC.class, Keys.class})
public class GetAsyncOperationActorTest {

    private GetAsyncOperationActor actor;
    private GetAsyncOperationMessage message;
    private IAsyncOperationCollection collection;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        collection = Mockito.mock(IAsyncOperationCollection.class);
        IKey collectionKey = Mockito.mock(IKey.class);
        String databaseOptionsKey = "key";
        Object databaseOptions = mock(Object.class);

        when(Keys.resolveByName(IAsyncOperationCollection.class.getCanonicalName())).thenReturn(collectionKey);
        when(IOC.resolve(eq(collectionKey), any(), any())).thenReturn(collection);

        IField collectionNameField = Mockito.mock(IField.class);
        IField databaseOptionsF = mock(IField.class);
        IKey collectionNameFieldKey = Mockito.mock(IKey.class);

        when(IOC.resolve(collectionNameFieldKey, "collectionName")).thenReturn(collectionNameField);
        when(IOC.resolve(collectionNameFieldKey, "databaseOptions")).thenReturn(databaseOptionsF);

        when(Keys.resolveByName(IField.class.getCanonicalName())).thenReturn(collectionNameFieldKey);
        when(IOC.resolve(collectionNameFieldKey, "collectionName")).thenReturn(collectionNameField);

        when(databaseOptionsF.in(any())).thenReturn(databaseOptionsKey);
        when(IOC.resolve(Keys.resolveByName(databaseOptionsKey))).thenReturn(databaseOptions);

        message = mock(GetAsyncOperationMessage.class);
        actor = new GetAsyncOperationActor(mock(IObject.class));
    }

    @Test
    public void ShouldReadOperationByToken()
        throws ReadValueException, GetAsyncOperationActorException, GetAsyncOperationException, ChangeValueException {

        String token = "token";
        when(message.getToken()).thenReturn(token);
        IObject asyncOperation = mock(IObject.class);
        when(collection.getAsyncOperation(token)).thenReturn(asyncOperation);

        actor.getOperation(message);

        verify(message).setAsyncOperation(asyncOperation);
    }

    @Test(expected = GetAsyncOperationActorException.class)
    public void ShouldThrowException_When_AsyncOperationIsNull()
        throws ReadValueException, GetAsyncOperationActorException, GetAsyncOperationException, ChangeValueException {

        when(collection.getAsyncOperation(anyString())).thenReturn(null);
        actor.getOperation(message);

        fail();
    }
}
