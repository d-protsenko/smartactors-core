package info.smart_tools.smartactors.actor.create_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

import java.time.LocalDateTime;

public interface AsyncOperation {

    void setToken(String token) throws ChangeValueException;
    void setExpiredTime(LocalDateTime expiredTime) throws ChangeValueException;
    void setIsDone(Boolean isDone) throws ChangeValueException;
    void setOperationData(AuthOperationData operationData) throws ChangeValueException;

}
