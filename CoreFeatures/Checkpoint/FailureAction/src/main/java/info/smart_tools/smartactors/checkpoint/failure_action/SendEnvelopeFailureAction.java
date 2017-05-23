package info.smart_tools.smartactors.checkpoint.failure_action;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;

/**
 * The action that sends a lost message wrapped into an envelope message to specified chain.
 */
public class SendEnvelopeFailureAction implements IAction<IObject> {
    private final Object targetChainId;
    private final IFieldName messageFieldName;
    private final IAction<IObject> backupAction;

    /**
     * The constructor.
     *
     * @param targetChainId       identifier of the chain where to send the envelope
     * @param messageFieldName    name of the envelope field where to put the message
     * @param backupAction        the action that should be executed if error occurs executing this action
     */
    public SendEnvelopeFailureAction(final Object targetChainId, final IFieldName messageFieldName, final IAction<IObject> backupAction) {
        this.targetChainId = targetChainId;
        this.messageFieldName = messageFieldName;
        this.backupAction = backupAction;
    }

    @Override
    public void execute(final IObject actingObject) throws ActionExecuteException, InvalidArgumentException {
        try {
            IObject envelope = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            envelope.setValue(messageFieldName, actingObject);
            MessageBus.send(envelope, targetChainId);
        } catch (ResolutionException | ChangeValueException | InvalidArgumentException | SendingMessageException e) {
            try {
                backupAction.execute(actingObject);
            } catch (Exception e1) {
                e.addSuppressed(e1);
            }

            throw new ActionExecuteException("Error occurred sending lost message.", e);
        }
    }
}
