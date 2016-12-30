package info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_container.exception.SendingMessageException;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerAction;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerActionExecutionException;
import info.smart_tools.smartactors.scheduler.interfaces.exceptions.SchedulerActionInitializationException;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.IQueryExecutor;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.exceptions.QueryExecutorInitializationException;

import java.util.Collection;

/**
 * Scheduler action that queries data using {@link IQueryExecutor a query executor} and sends it to a statistics chain.
 */
public class QuerySensorSchedulerAction implements ISchedulerAction {
    private final IFieldName queryExecutorFieldName;
    private final IFieldName statisticsChainFieldName;
    private final IFieldName dataFieldName;

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving any dependency
     */
    public QuerySensorSchedulerAction()
            throws ResolutionException {
        queryExecutorFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "queryExecutor");
        statisticsChainFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "statisticsChain");
        dataFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "data");
    }

    @Override
    public void init(final ISchedulerEntry entry, final IObject args) throws SchedulerActionInitializationException {
        try {
            Object queryExecutorDependency = args.getValue(queryExecutorFieldName);
            IQueryExecutor queryExecutor = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), queryExecutorDependency));

            queryExecutor.init(entry, args);

            entry.getState().setValue(queryExecutorFieldName, queryExecutorDependency);
            entry.getState().setValue(statisticsChainFieldName, args.getValue(statisticsChainFieldName));
        } catch (ResolutionException | ReadValueException | ChangeValueException | InvalidArgumentException
                | QueryExecutorInitializationException e) {
            throw new SchedulerActionInitializationException("Error occurred initializing query sensor scheduler action.", e);
        }
    }

    @Override
    public void execute(final ISchedulerEntry entry) throws SchedulerActionExecutionException {
        try {
            Object queryExecutorDependency = entry.getState().getValue(queryExecutorFieldName);
            IQueryExecutor queryExecutor = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), queryExecutorDependency));
            Collection<? extends Number> data = queryExecutor.execute(entry);

            IObject message = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
            message.setValue(dataFieldName, data);
            Object chainId = IOC.resolve(Keys.getOrAdd("chain_id_from_map_name"), entry.getState().getValue(statisticsChainFieldName));
            MessageBus.send(message, chainId);
        } catch (ReadValueException | InvalidArgumentException | QueryExecutionException | ResolutionException | ChangeValueException
                | SendingMessageException e) {
            throw new SchedulerActionExecutionException("Error occurred querying or sending statistics.", e);
        }
    }
}
