package info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.query_executors;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerEntry;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.IQueryExecutor;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.exceptions.QueryExecutionException;
import info.smart_tools.smartactors.statistics.sensors.scheduled_query_sensor.interfaces.exceptions.QueryExecutorInitializationException;

import java.util.Collection;

/**
 *
 */
public class DatabaseCountQueryExecutor implements IQueryExecutor {
    @Override
    public void init(final ISchedulerEntry entry, final IObject args) throws QueryExecutorInitializationException {
        // TODO::
    }

    @Override
    public Collection<? extends Number> execute(final ISchedulerEntry entry) throws QueryExecutionException {
        // TODO::
        return null;
    }
}
