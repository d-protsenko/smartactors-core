package info.smart_tools.smartactors.statistics.sensors.embedded_sensor.interfaces;

import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

import java.util.Collection;

/**
 * @param <TPeriodState> type of a object representing state of observation for a single period
 */
public interface IEmbeddedSensorStrategy<TPeriodState> {
    /**
     * Start new observation period.
     *
     * @return the period observation state
     */
    TPeriodState initPeriod();

    /**
     * Record information about a message passing the sensor.
     *
     * @param period       the current period observation state
     * @param processor    the message processor processing the message
     * @param time         the time (in milliseconds, as returned by {@link System#currentTimeMillis()}) when the message was received
     */
    void updatePeriod(TPeriodState period, IMessageProcessor processor, long time);

    /**
     * Get all the measurements from given period state.
     *
     * @param period    the period observation state
     * @return collection of measurements
     */
    Collection<? extends Number> extractPeriod(TPeriodState period);
}
