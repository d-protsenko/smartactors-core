package info.smart_tools.smartactors.actors.create_async_operation;

import info.smart_tools.smartactors.actors.create_async_operation.exception.CreateAsyncOperationActorException;
import info.smart_tools.smartactors.actors.create_async_operation.wrapper.CreateAsyncOperationMessage;
import info.smart_tools.smartactors.core.async_operation_collection.IAsyncOperationCollection;
import info.smart_tools.smartactors.core.async_operation_collection.exception.CreateAsyncOperationException;
import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Actor for creation asynchronous operation
 */
public class CreateAsyncOperationActor {

    private IAsyncOperationCollection collection;

    //TODO:: this format should be setted for whole project?
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    /**
     * Constructor needed for registry actor
     * @param params iobject
     * @throws CreateAsyncOperationActorException if any error is occurred
     */
    public CreateAsyncOperationActor(final IObject params) throws CreateAsyncOperationActorException {
        try {
            IField collectionNameField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "collectionName");
            collection = IOC.resolve(
                Keys.getOrAdd(IAsyncOperationCollection.class.getCanonicalName()), (String) collectionNameField.in(params)
            );
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new CreateAsyncOperationActorException("Can't read collection name from message", e);
        } catch (ResolutionException e) {
            throw new CreateAsyncOperationActorException("Can't get key or resolve dependency", e);
        }
    }

    /**
     * Generates token and creates asynchronous operation.
     * Sets token to session and response.
     * @param message {
     *                "sessionId": "session identifier for save as async data",
     *                "expiredTime": "TTL for async operation"
     * }
     * @throws CreateAsyncOperationActorException for creation error
     */
    public void create(final CreateAsyncOperationMessage message) throws CreateAsyncOperationActorException {

        try {
            //TODO:: move generate to util class and add server number
            String token = String.valueOf(UUID.randomUUID());
            Integer amountOfHoursToExpireFromNow = message.getExpiredTime();
            String expiredTime = LocalDateTime.now().plusHours(amountOfHoursToExpireFromNow).format(FORMATTER);
            message.setSessionIdInData(message.getSessionId());
            IObject authOperationData = message.getOperationData();
            collection.createAsyncOperation(authOperationData, token, expiredTime);

            //NOTE: this setter should set token to session and to response!
            message.setAsyncOperationToken(token);

            List<String> availableTokens = message.getOperationTokens();
            if (availableTokens == null) {
                message.setOperationTokens(Collections.singletonList(token));
            } else {
                availableTokens.add(token);
                message.setOperationTokens(availableTokens);
            }
        } catch (ReadValueException | ChangeValueException | CreateAsyncOperationException e) {
            throw new CreateAsyncOperationActorException("Can't create async operation.", e);
        }
    }
}
