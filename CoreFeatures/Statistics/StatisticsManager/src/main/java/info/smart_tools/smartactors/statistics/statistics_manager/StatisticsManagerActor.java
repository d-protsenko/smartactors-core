package info.smart_tools.smartactors.statistics.statistics_manager;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.exceptions.ConfigurationProcessingException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.statistics.sensors.interfaces.ISensorHandle;
import info.smart_tools.smartactors.statistics.sensors.interfaces.exceptions.SensorShutdownException;
import info.smart_tools.smartactors.statistics.statistics_manager.exceptions.CommandExecutionException;
import info.smart_tools.smartactors.statistics.statistics_manager.exceptions.CommandNotFoundException;
import info.smart_tools.smartactors.statistics.statistics_manager.wrappers.StatisticsCommandWrapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class StatisticsManagerActor {
    /** Interface for a single command */
    @FunctionalInterface
    private interface ICommand {
        Object execute(Object arg) throws CommandExecutionException, InvalidArgumentException;
    }

    /** Class that holds information about a single sensor */
    private class SensorInfo {
        private final IObject originalConfig;
        private final ISensorHandle handle;
        private final String chainName;
        private final Map<String, IObject> collectors;
        private final String originalDependency;

        SensorInfo(final String originalDependency, final IObject conf, final ISensorHandle handle, final String chainName) {
            this.originalDependency = originalDependency;
            this.originalConfig = conf;
            this.handle = handle;
            this.chainName = chainName;
            this.collectors = new HashMap<>();
        }

        String getChainName() {
            return chainName;
        }

        ISensorHandle getHandle() {
            return handle;
        }

        Map<String, IObject> getConnectedCollectors() {
            return collectors;
        }

        IObject getOriginalConfig() {
            return originalConfig;
        }

        String getOriginalDependency() {
            return originalDependency;
        }
    }

    /** Class that holds information about a single data collector */
    private class CollectorInfo {
        private final String objectName;
        private final IObject originalConfig;

        CollectorInfo(final String objectName, final IObject originalConfig) {
            this.objectName = objectName;
            this.originalConfig = originalConfig;
        }

        String getObjectName() {
            return objectName;
        }

        IObject getOriginalConfig() {
            return originalConfig;
        }
    }

    private final IFieldName chainsSectionFieldName;
    private final IFieldName objectsSectionFieldName;
    private final IFieldName chainIdFieldName;
    private final IFieldName exceptionalFieldName;
    private final IFieldName chainStepsFieldName;
    private final IFieldName dependencyFieldName;
    private final IFieldName argsFieldName;
    private final IFieldName idFieldName;
    private final IFieldName sensorFieldName;
    private final IFieldName collectorFieldName;
    private final IFieldName stepConfigFieldName;
    private final IFieldName objectNameFieldName;
    private final IFieldName targetFieldName;

    private final Map<String, SensorInfo> sensors;
    private final Map<String, CollectorInfo> collectors;
    private final Set<String> invalidSensorChains;
    private final Map<String, ICommand> commands;

    private IObject toConfigObject(final IObject object)
            throws SerializeException, ResolutionException {
        String serialized = object.serialize();
        return IOC.resolve(Keys.getOrAdd("configuration object"), serialized);
    }

    private void invalidateChainFor(final String sensorId) {
        invalidSensorChains.add(sensorId);
    }

    private IObject createChainConfigFor(final String sensorId)
            throws ResolutionException, ChangeValueException, InvalidArgumentException {
        IObject chainConfig = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));
        chainConfig.setValue(chainIdFieldName, sensors.get(sensorId).getChainName());
        chainConfig.setValue(exceptionalFieldName, Collections.EMPTY_LIST);

        List<IObject> steps = new ArrayList<>(sensors.get(sensorId).getConnectedCollectors().size());

        for (Map.Entry<String, IObject> entry : sensors.get(sensorId).getConnectedCollectors().entrySet()) {
            IObject step = entry.getValue();
            steps.add(step);
        }

        chainConfig.setValue(chainStepsFieldName, steps);
        return chainConfig;
    }

    private void rebuildChains()
            throws ChangeValueException, ResolutionException, InvalidArgumentException, ConfigurationProcessingException,
            SerializeException {
        IObject rootObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        List<IObject> chainsSection = new ArrayList<>(invalidSensorChains.size());

        for (String id : invalidSensorChains) {
            chainsSection.add(createChainConfigFor(id));
        }

        rootObject.setValue(chainsSectionFieldName, chainsSection);

        IConfigurationManager configurationManager = IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

        configurationManager.applyConfig(toConfigObject(rootObject));

        invalidSensorChains.clear();
    }

    private void createCollectorObject(final String objectName, final IObject objectConfig)
            throws ChangeValueException, InvalidArgumentException, ConfigurationProcessingException, ResolutionException,
            SerializeException {
        objectConfig.setValue(objectNameFieldName, objectName);

        IObject rootConfigObject = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

        rootConfigObject.setValue(objectsSectionFieldName, Collections.singletonList(objectConfig));

        IConfigurationManager configurationManager = IOC.resolve(Keys.getOrAdd(IConfigurationManager.class.getCanonicalName()));

        configurationManager.applyConfig(toConfigObject(rootConfigObject));
    }

    private String generateChainNameForSensor(final String sensorId) {
        return MessageFormat.format("sensor-chain/{0}-{1}", sensorId, UUID.randomUUID().toString());
    }

    private String generateCollectorObjectName(final String collectorId) {
        return MessageFormat.format("data-collector/{0}-{1}", collectorId, UUID.randomUUID().toString());
    }

    /**
     * The constructor.
     *
     * @throws ResolutionException if error occurs resolving anu dependency
     */
    public StatisticsManagerActor()
            throws ResolutionException {
        chainsSectionFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "maps");
        objectsSectionFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "objects");
        chainIdFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
        exceptionalFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "exceptional");
        chainStepsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "steps");
        dependencyFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "dependency");
        argsFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "args");
        idFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "id");
        sensorFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "sensor");
        collectorFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collector");
        stepConfigFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "stepConfig");
        objectNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
        targetFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "target");

        sensors = new HashMap<>();
        collectors = new HashMap<>();
        invalidSensorChains = new HashSet<>();
        commands = new HashMap<>();

        commands.put("createSensor", this::cmdCreateSensor);
        commands.put("shutdownSensor", this::cmdShutdownSensor);
        commands.put("link", this::cmdLink);
        commands.put("unlink", this::cmdUnlink);
        commands.put("enumSensors", this::cmdEnumSensors);
        commands.put("createCollector", this::cmdCreateCollector);
        commands.put("enumCollectors", this::cmdEnumCollectors);
    }

    /**
     * Execute a command.
     *
     * @param message    the message wrapper
     * @throws ReadValueException if error occurs reading the message
     * @throws CommandNotFoundException if the command required is not exist
     * @throws ChangeValueException if error occurs writing back the result
     */
    public void executeCommand(final StatisticsCommandWrapper message)
            throws ReadValueException, CommandNotFoundException, ChangeValueException {
        ICommand command = commands.get(message.getCommand());

        if (null == command) {
            throw new CommandNotFoundException(MessageFormat.format("No such command: ''{0}''", message.getCommand()));
        }

        try {
            message.setCommandResult(command.execute(message.getCommandArguments()));
        } catch (CommandExecutionException | InvalidArgumentException e) {
            message.setException(e);
        }
    }

    private Object cmdCreateSensor(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            IObject args = (IObject) arg;

            String id = (String) args.getValue(idFieldName);

            if (sensors.containsKey(id)) {
                throw new InvalidArgumentException(MessageFormat.format("Sensor named ''{0}'' already exist.", id));
            }

            ISensorHandle handle = IOC.resolve(
                    IOC.resolve(IOC.getKeyForKeyStorage(), args.getValue(dependencyFieldName)),
                    args.getValue(argsFieldName)
            );

            sensors.put(id, new SensorInfo(
                    (String) args.getValue(dependencyFieldName),
                    (IObject) args.getValue(argsFieldName),
                    handle,
                    generateChainNameForSensor(id)));

            invalidateChainFor(id);
            rebuildChains();

            return id;
        } catch (ClassCastException | ReadValueException | InvalidArgumentException | ResolutionException | ChangeValueException
                | ConfigurationProcessingException | SerializeException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdLink(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            IObject args = (IObject) arg;
            String sensorId = (String) args.getValue(sensorFieldName);
            String collectorId = (String) args.getValue(collectorFieldName);
            IObject stepConf = (IObject) args.getValue(stepConfigFieldName);

            stepConf.setValue(targetFieldName, collectors.get(collectorId).getObjectName());

            sensors.get(sensorId).getConnectedCollectors().put(collectorId, stepConf);

            invalidateChainFor(sensorId);
            rebuildChains();

            return "OK";
        } catch (ReadValueException | InvalidArgumentException | ConfigurationProcessingException | ChangeValueException
                | ResolutionException | SerializeException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdUnlink(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            IObject args = (IObject) arg;
            String sensorId = (String) args.getValue(sensorFieldName);
            String collectorId = (String) args.getValue(collectorFieldName);

            if (null == sensors.get(sensorId).getConnectedCollectors().remove(collectorId)) {
                return "NOT EXIST";
            }

            invalidateChainFor(sensorId);
            rebuildChains();

            return "OK";
        } catch (ReadValueException | InvalidArgumentException | ConfigurationProcessingException | ChangeValueException
                | ResolutionException | SerializeException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdShutdownSensor(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            String sensorId = (String) arg;

            if (!sensors.containsKey(sensorId)) {
                throw new CommandExecutionException("No such sensor.");
            }

            sensors.get(sensorId).getHandle().shutdown();

            // TODO:: Remove chain

            sensors.remove(sensorId);

            return "OK";
        } catch (SensorShutdownException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdEnumSensors(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            List<IObject> sensorsList = new ArrayList<>(sensors.size());

            for (Map.Entry<String, SensorInfo> entry : sensors.entrySet()) {
                IObject sensorView = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                sensorView.setValue(idFieldName, entry.getKey());
                sensorView.setValue(dependencyFieldName, entry.getValue().getOriginalDependency());
                sensorView.setValue(argsFieldName, entry.getValue().getOriginalConfig());

                sensorsList.add(sensorView);
            }

            return sensorsList;
        } catch (ChangeValueException | InvalidArgumentException | ResolutionException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdCreateCollector(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            IObject args = (IObject) arg;
            String collectorId = (String) args.getValue(idFieldName);
            IObject config = (IObject) args.getValue(argsFieldName);

            String name = generateCollectorObjectName(collectorId);

            createCollectorObject(name, config);

            collectors.put(collectorId, new CollectorInfo(name, config));

            return "OK";
        } catch (ReadValueException | ChangeValueException | InvalidArgumentException | ResolutionException | SerializeException
                | ConfigurationProcessingException e) {
            throw new CommandExecutionException(e);
        }
    }

    private Object cmdEnumCollectors(final Object arg)
            throws CommandExecutionException, InvalidArgumentException {
        try {
            List<IObject> collectorsList = new ArrayList<>(collectors.size());

            for (Map.Entry<String, CollectorInfo> entry : collectors.entrySet()) {
                IObject sensorView = IOC.resolve(Keys.getOrAdd(IObject.class.getCanonicalName()));

                sensorView.setValue(idFieldName, entry.getKey());
                sensorView.setValue(argsFieldName, entry.getValue().getOriginalConfig());

                collectorsList.add(sensorView);
            }

            return collectorsList;
        } catch (ChangeValueException | InvalidArgumentException | ResolutionException e) {
            throw new CommandExecutionException(e);
        }
    }
}
