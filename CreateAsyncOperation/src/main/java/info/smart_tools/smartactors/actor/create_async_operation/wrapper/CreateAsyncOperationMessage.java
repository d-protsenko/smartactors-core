package info.smart_tools.smartactors.actor.create_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

public interface CreateAsyncOperationMessage {

    String getAsyncOperationToken() throws ReadValueException;
    String getSessionId() throws ReadValueException;
    //TODO:: clarify about names
    void setAsyncOperationSessionToken(String token) throws ChangeValueException;
    void setAsyncOperationToken(String token) throws ChangeValueException;
}
