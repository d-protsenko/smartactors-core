package info.smart_tools.smartactors.actor.create_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

public interface AuthOperationData {

    void setSessionId(String sessionId) throws ChangeValueException;
}
