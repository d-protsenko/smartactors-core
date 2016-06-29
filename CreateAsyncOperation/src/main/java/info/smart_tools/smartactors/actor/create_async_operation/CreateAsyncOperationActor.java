package info.smart_tools.smartactors.actor.create_async_operation;

import info.smart_tools.smartactors.actor.create_async_operation.wrapper.AsyncOperation;
import info.smart_tools.smartactors.actor.create_async_operation.wrapper.AuthOperationData;
import info.smart_tools.smartactors.actor.create_async_operation.wrapper.CreateAsyncOperationMessage;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;

public class CreateAsyncOperationActor {

    public CreateAsyncOperationActor() {
    }

    public void create(final CreateAsyncOperationMessage message) {

        try {
            String token = message.getAsyncOperationToken();
            IAsyncOperationCollection collection = IOC.resolve(Keys.getOrAdd(IAsyncOperationCollection.class.toString()));
            AsyncOperation asyncOperation = IOC.resolve(Keys.getOrAdd(AsyncOperation.class.toString()));
            asyncOperation.setToken(token);
            asyncOperation.setExpiredTime(LocalDateTime.now());
            asyncOperation.setIsDone(false);
            AuthOperationData authOperationData = IOC.resolve(Keys.getOrAdd(AuthOperationData.class.toString()));
            authOperationData.setSessionId(message.getSessionId());
            //TODO:: Think about it, maybe IObject will be better and then move AsyncOperation interafce to AsyncCollection module?
            asyncOperation.setOperationData(authOperationData);
            collection.insert(asyncOperation);

            message.setAsyncOperationSessionToken(token);
            message.setAsyncOperationToken(token);
        } catch (ReadValueException e) {
            //TODO:: handle usususususus
        } catch (ResolutionException | ChangeValueException e) {
            e.printStackTrace();
        }
    }
}
