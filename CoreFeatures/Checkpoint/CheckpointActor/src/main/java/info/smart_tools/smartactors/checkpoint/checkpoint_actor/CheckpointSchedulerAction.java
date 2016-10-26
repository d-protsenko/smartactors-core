package info.smart_tools.smartactors.checkpoint.checkpoint_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.checkpoint.interfaces.IRecoverStrategy;
import info.smart_tools.smartactors.checkpoint.interfaces.exceptions.RecoverStrategyExecutionException;
import info.smart_tools.smartactors.checkpoint.interfaces.exceptions.RecoverStrategyInitializationException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerAction;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerActionExecutionException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerActionInitializationException;

/**
 * Action executed when checkpoint entry fires.
 */
public class CheckpointSchedulerAction implements ISchedulerAction {
    private final IFieldName recoverFieldName;
    private final IFieldName strategyFieldName;
    private final IFieldName recoverStrategyFieldName;
    private final IFieldName messageFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependencies
     */
    public CheckpointSchedulerAction()
            throws ResolutionException {
        recoverFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "recover");
        strategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "strategy");
        recoverStrategyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "recoverStrategy");
        messageFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "message");
    }

    @Override
    public void init(final ISchedulerEntry entry, final IObject args)
            throws SchedulerActionInitializationException {
        try {
            IObject recoverConfig = (IObject) args.getValue(recoverFieldName);
            String recoverStrategyId = (String) args.getValue(strategyFieldName);
            IObject message = (IObject) args.getValue(messageFieldName);

            if (null == message) {
                throw new SchedulerActionInitializationException(
                        "Checkpoint scheduler action arguments should contain message object.", null);
            }

            IRecoverStrategy strategy = IOC.resolve(Keys.getOrAdd(recoverStrategyId));

            strategy.init(entry.getState(), recoverConfig);

            entry.getState().setValue(recoverStrategyFieldName, recoverStrategyId);
        } catch (ReadValueException | InvalidArgumentException | ResolutionException | RecoverStrategyInitializationException
                | ChangeValueException e) {
            throw new SchedulerActionInitializationException("Error occurred initializing checkpoint action.", e);
        }
    }

    @Override
    public void execute(final ISchedulerEntry entry) throws SchedulerActionExecutionException {
        try {
            IRecoverStrategy recoverStrategy = IOC.resolve(IOC.resolve(
                    IOC.getKeyForKeyStorage(),
                    entry.getState().getValue(recoverStrategyFieldName)
            ));

            Object chainId = recoverStrategy.chooseRecoveryChain(entry.getState());

            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));

            MessageBus.send(cloneMessage((IObject) entry.getState().getValue(messageFieldName)), chainStorage.resolve(chainId));
        } catch (ResolutionException | ReadValueException | InvalidArgumentException | RecoverStrategyExecutionException
                | SerializeException | ChainNotFoundException | SendingMessageException e) {
            throw new SchedulerActionExecutionException("Error occurred executing ch", e);
        }
    }

    private IObject cloneMessage(final IObject original)
            throws SerializeException, ResolutionException {
        String serialized = original.serialize();
        return IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()), serialized);
    }
}
