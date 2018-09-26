package info.smart_tools.smartactors.feature_management.feature_manager_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.exception.FeatureManagementException;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper.AddFeatureWrapper;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper.FeatureManagerStateWrapper;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper.OnFeatureLoadedWrapper;
import info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper.OnFeatureStepCompletedWrapper;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.exceptions.ChainNotFoundException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.AsynchronousOperationException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageProcessorProcessException;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Actor that manages process of features downloading, unpacking and loading.
 */
public class FeatureManagerActor {

    private static final int DEFAULT_STACK_DEPTH = 5;

    private static final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_TIME;

    private Map<IMessageProcessor, Set<IFeature>> mainProcesses;
    private Map<IMessageProcessor, Set<IFeature>> mainProcessesForInfo;
    private Map<IMessageProcessor, IFeature> featureProcess;

    private Map<Object, IFeature> loadedFeatures;
    private Map<Object, IFeature> failedFeatures;
    private Map<Object, IFeature> processingFeatures;

    private final IFieldName loadedFeatureFN;
    private final IFieldName failedFeatureFN;
    private final IFieldName processingFeatureFN;
    private final IFieldName featureProcessFN;
    private final IFieldName featureFN;
    private final IFieldName afterFeaturesCallbackQueueFN;
    private final IFieldName startTimeOfLoadingFeatureGroupFN;

    private final static String TASK_QUEUE_IOC_NAME = "task_queue";
    private final static String CHAIN_ID_STORAGE_STRATEGY_NAME = "chain_id_from_map_name";
    private final static String IOBJECT_FACTORY_STRATEGY_NAME = "info.smart_tools.smartactors.iobject.iobject.IObject";
    private final static String FIELD_NAME_FACTORY_STARTEGY_NAME =
            "info.smart_tools.smartactors.iobject.ifield_name.IFieldName";
    private final static String MESSAGE_PROCESSOR_SEQUENCE_FACTORY_STRATEGY_NAME =
            "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence";
    private final static String MESSAGE_PROCESSOR_FACTORY_STRATEGY_NAME =
            "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor";

    /**
     * Default constructor
     * @throws ResolutionException if any errors occurred on resolution IOC dependencies
     */
    public FeatureManagerActor()
            throws ResolutionException {
        this.mainProcesses = new ConcurrentHashMap<>();
        this.mainProcessesForInfo = new ConcurrentHashMap<>();
        this.featureProcess = new ConcurrentHashMap<>();

        this.loadedFeatures = new ConcurrentHashMap<>();
        this.failedFeatures = new ConcurrentHashMap<>();
        this.processingFeatures = new ConcurrentHashMap<>();

        this.loadedFeatureFN =                  IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "loadedFeatures"
        );
        this.failedFeatureFN =                  IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "failedFeatures"
        );
        this.processingFeatureFN =              IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "processingFeatures"
        );
        this.featureProcessFN =                 IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "featureProcess"
        );
        this.featureFN =                        IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "feature"
        );
        this.afterFeaturesCallbackQueueFN =     IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "afterFeaturesCallbackQueue"
        );
        this.startTimeOfLoadingFeatureGroupFN = IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "startTimeOfLoadingFeatureGroup"
        );
    }

    /**
     * Adds features to the processing
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void addFeatures(final AddFeatureWrapper wrapper)
            throws FeatureManagementException {
        try {
            LocalTime startLoadingTime = LocalTime.now();
            Set<IFeature> features = new HashSet<>(wrapper.getFeatures());
            Set<IFeature> featuresForInfo = new HashSet<>(wrapper.getFeatures());
            IMessageProcessor mp = wrapper.getMessageProcessor();
            mp.getContext().setValue(this.startTimeOfLoadingFeatureGroupFN, startLoadingTime);

            IQueue<ITask> queue = IOC.resolve(Keys.getOrAdd(TASK_QUEUE_IOC_NAME));
            String scatterChainName = wrapper.getScatterChainName();
            Object chainId = IOC.resolve(Keys.getOrAdd(CHAIN_ID_STORAGE_STRATEGY_NAME), scatterChainName);
            IChainStorage chainStorage = IOC.resolve(Keys.getOrAdd(IChainStorage.class.getCanonicalName()));
            int stackDepth = DEFAULT_STACK_DEPTH;
            IReceiverChain scatterChain = chainStorage.resolve(chainId);

            IQueue afterFeaturesCallbackQueue = IOC.resolve(Keys.getOrAdd(IQueue.class.getCanonicalName()));

            int count = 0;
            for (IFeature feature : features) {
                Object featureName = feature.getName();
                if (
                        null == this.processingFeatures.get(featureName) &&
                        null == this.loadedFeatures.get(featureName)
                ) {
                    this.processingFeatures.put(feature.getName(), feature);
                    IMessageProcessingSequence processingSequence = IOC.resolve(
                            Keys.getOrAdd(MESSAGE_PROCESSOR_SEQUENCE_FACTORY_STRATEGY_NAME), stackDepth, scatterChain
                    );
                    IMessageProcessor messageProcessor = IOC.resolve(
                            Keys.getOrAdd(MESSAGE_PROCESSOR_FACTORY_STRATEGY_NAME), queue, processingSequence
                    );
                    IObject message = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME));
                    message.setValue(this.loadedFeatureFN, this.loadedFeatures);
                    message.setValue(this.failedFeatureFN, this.failedFeatures);
                    message.setValue(this.processingFeatureFN, this.processingFeatures);
                    message.setValue(this.featureProcessFN, this.featureProcess);
                    message.setValue(this.featureFN, feature);
                    message.setValue(this.afterFeaturesCallbackQueueFN, afterFeaturesCallbackQueue);
                    IObject context = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME));
                    messageProcessor.process(message, context);
                    ++count;
                }
            }
            if (count > 0) {
                mp.pauseProcess();
                this.mainProcesses.put(mp, features);
                this.mainProcessesForInfo.put(mp, featuresForInfo);
            }
        } catch (
                ReadValueException |
                ChangeValueException |
                InvalidArgumentException |
                ResolutionException |
                ChainNotFoundException |
                MessageProcessorProcessException |
                AsynchronousOperationException e
        ) {
            throw new FeatureManagementException(e);
        }
    }

    /**
     * Ends feature processing
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void onFeatureLoaded(final OnFeatureLoadedWrapper wrapper)
            throws FeatureManagementException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();

            this.processingFeatures.remove(feature.getName());
            if (feature.isFailed()) {
                this.failedFeatures.put(feature.getName(), feature);

            } else {
                this.loadedFeatures.put(feature.getName(), feature);
                this.failedFeatures.remove(feature.getName());
                removeLoadedFeatureFromDependencies(feature);
                checkAndRunConnectedFeatures();
            }

            this.mainProcesses.forEach((k, v) -> {
                v.remove(feature);
            });
            Collection<IMessageProcessor> needContinueProcesses = new HashSet<>();
            this.mainProcesses.forEach((k, v) -> {
                if (v.isEmpty()) {
                    needContinueProcesses.add(k);
                    this.mainProcesses.remove(k);
                }
            });
            needContinueProcesses.forEach((mp) -> {
                try {

                    IQueue<ITask> afterFeatureCallbackQueue = wrapper.getAfterFeaturesCallbackQueue();
                    ITask task;
                    while (null != (task = afterFeatureCallbackQueue.tryTake())) {
                        try {
                            task.execute();
                        } catch (TaskExecutionException e) {
                            throw new ActionExecuteException(e);
                        }
                    }

                    LocalTime startLoadingTime = (LocalTime) mp.getContext().getValue(
                            this.startTimeOfLoadingFeatureGroupFN
                    );
                    mp.continueProcess(null);
                    Duration elapsedInterval = Duration.between(startLoadingTime, LocalTime.now());
                    LocalTime elapsedTimeToLocalTime = LocalTime.ofNanoOfDay(elapsedInterval.toNanos());

                    System.out.println("\n\n");
                    System.out.println(
                            "[INFO] Feature group has been loaded: " +
                                    this.mainProcessesForInfo.get(mp).stream().map(
                                            a -> "\n" + a.getName() + " - " + (!a.isFailed() ? "(OK)" : "(Failed)")
                                    ).collect(Collectors.toList())
                    );
                    System.out.println("[INFO] elapsed time - " + elapsedTimeToLocalTime.format(df) + ".");
                    System.out.println("\n\n");
                    this.mainProcessesForInfo.remove(mp);
                } catch (
                        InvalidArgumentException |
                        AsynchronousOperationException |
                        ReadValueException |
                        ActionExecuteException e
                ) {
                    throw new RuntimeException(e);
                }
            });
            checkUnresolved();

        } catch (ReadValueException e) {
            throw new FeatureManagementException("Feature should not be null.");
        }
    }

    /**
     * Ends step of feature processing
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void onFeatureStepCompleted(final OnFeatureStepCompletedWrapper wrapper)
            throws FeatureManagementException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
            checkAndRunConnectedFeatures();
            for (IFeature loadedFeature : this.loadedFeatures.values()) {
                if (null != feature.getDependencies()) {
                    feature.getDependencies().remove(loadedFeature.getGroupId() + ":" + loadedFeature.getName());
                }
            }
            if (null != feature.getDependencies() && !feature.getDependencies().isEmpty()) {
                IMessageProcessor mp = wrapper.getMessageProcessor();
                mp.pauseProcess();
                this.featureProcess.put(mp, feature);
            }
        } catch (ReadValueException | AsynchronousOperationException e) {
            throw new FeatureManagementException(e);
        }
    }

    /**
     * Gets state of added features
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void getState(final FeatureManagerStateWrapper wrapper)
            throws FeatureManagementException {
        try {
            wrapper.setLoadedFeatures(this.loadedFeatures.values());
            wrapper.setFailedFeatures(this.failedFeatures.values());
            wrapper.setProcessingFeatures(this.processingFeatures.values());
            wrapper.setFrozenFeatureProcesses(this.featureProcess);
            wrapper.setFrozenRequests(this.mainProcesses);
        } catch (ChangeValueException e) {
            throw new FeatureManagementException("Could not set parameter to IObject.", e);
        }
    }

    private void checkAndRunConnectedFeatures() {
        Collection<IMessageProcessor> needContinueFeatures = new HashSet<>();
        this.featureProcess.forEach((k, v) -> {
            if (v.getDependencies().isEmpty()) {
                needContinueFeatures.add(k);
                this.featureProcess.remove(k);
            }
        });
        needContinueFeatures.forEach((mp) -> {
            try {
                mp.continueProcess(null);
            } catch (AsynchronousOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void removeLoadedFeatureFromDependencies(final IFeature loadedFeature)
            throws FeatureManagementException {
        for (IFeature feature : this.processingFeatures.values()) {
            if (null != feature.getDependencies()) {
                feature.getDependencies().remove(loadedFeature.getGroupId() + ":" + loadedFeature.getName());
            }
        }
    }

    private void checkUnresolved() {
        int minDependencies = this.processingFeatures
                .values()
                .stream()
                .filter(feature -> null != feature.getDependencies())
                .map(f -> f.getDependencies().size())
                .min(Integer::compareTo)
                .orElse(0);
        if (
                !this.processingFeatures.isEmpty()
                && minDependencies > 0
        ) {
            Set<String> unresolved = this.processingFeatures
                    .values()
                    .stream()
                    .filter(feature -> null != feature.getDependencies())
                    .map(IFeature::getDependencies)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            unresolved.removeAll(
                    this.processingFeatures
                            .values()
                            .stream()
                            .map(f -> f.getGroupId() + ":" + f.getName())
                            .collect(Collectors.toSet())
            );

            System.out.println("[INFO] The server needs the following features to continue: " +
                    unresolved.stream().map(a -> "\n\t\t" + a).collect(Collectors.toList())
            );
        }
    }
}
