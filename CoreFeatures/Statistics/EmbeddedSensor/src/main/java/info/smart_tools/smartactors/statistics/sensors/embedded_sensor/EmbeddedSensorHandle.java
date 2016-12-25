package info.smart_tools.smartactors.statistics.sensors.embedded_sensor;

import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.statistics.sensors.interfaces.ISensorHandle;
import info.smart_tools.smartactors.statistics.sensors.interfaces.exceptions.SensorShutdownException;

/**
 *
 */
public class EmbeddedSensorHandle implements ISensorHandle {
    private final IChainStorage chainStorage;
    private final Object chainId;
    private final Object chainModificationId;

    public EmbeddedSensorHandle(final IChainStorage chainStorage, final Object chainId, final Object chainModificationId) {
        this.chainStorage = chainStorage;
        this.chainId = chainId;
        this.chainModificationId = chainModificationId;
    }

    @Override
    public void shutdown() throws SensorShutdownException {
        throw new SensorShutdownException("Shutdown procedure is not implemented for embedded sensors.");
    }
}
