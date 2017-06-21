package info.smart_tools.smartactors.statistics.sensors.embedded_sensor.utils;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;

/**
 * Strategy that creates a composite receiver of a receiver from original chain and a sensor receiver the way the sensor receiver receives
 * message before the original receiver.
 *
 * <p>
 *     This strategy is meant to be used with "chain modification: replace receivers" strategy.
 * </p>
 */
public class PrependSensorReceiverStrategy implements IResolveDependencyStrategy {
    @Override
    public <T> T resolve(final Object... args) throws ResolveDependencyStrategyException {
        final IMessageReceiver originalReceiver = (IMessageReceiver) args[0];
        final IMessageReceiver sensorReceiver = (IMessageReceiver) args[1];

        if (originalReceiver == null) {
            return (T) sensorReceiver;
        }

        return (T) new CompositeSensorReceiver(sensorReceiver, originalReceiver);
    }
}
