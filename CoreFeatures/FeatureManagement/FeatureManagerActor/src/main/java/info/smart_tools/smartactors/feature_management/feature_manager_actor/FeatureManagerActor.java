package info.smart_tools.smartactors.feature_management.feature_manager_actor;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.class_management.class_loader_management.VersionManager;
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

import java.util.*;
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

    private Map<IMessageProcessor, Set<IFeature>> requestProcesses;
    private Map<IMessageProcessor, Set<IFeature>> requestProcessesForInfo;
    private Map<IMessageProcessor, IFeature> featureProcesses;

    private Map<Object, IFeature> loadedFeatures;
    private Map<Object, IFeature> failedFeatures;
    private Map<Object, IFeature> featuresInProgress;

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
    private final static String FEATURE_NAME_DELIMITER = ":";

    /**
     * Default constructor
     *
     * @throws ResolutionException if any errors occurred on resolution IOC dependencies
     */
    public FeatureManagerActor()
            throws ResolutionException {
        this.requestProcesses = new ConcurrentHashMap<>();
        this.requestProcessesForInfo = new ConcurrentHashMap<>();
        this.featureProcesses = new ConcurrentHashMap<>();

        this.loadedFeatures = new ConcurrentHashMap<>();
        this.failedFeatures = new ConcurrentHashMap<>();
        this.featuresInProgress = new ConcurrentHashMap<>();

        this.featureFN = IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "feature"
        );
        this.afterFeaturesCallbackQueueFN = IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "afterFeaturesCallbackQueue"
        );
        this.startTimeOfLoadingFeatureGroupFN = IOC.resolve(
                Keys.getOrAdd(FIELD_NAME_FACTORY_STARTEGY_NAME), "startTimeOfLoadingFeatureGroup"
        );
    }

    /**
     * Adds features to the processing
     *
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
            VersionManager.setCurrentContext(null);
            IReceiverChain scatterChain = chainStorage.resolve(chainId);

            IQueue afterFeaturesCallbackQueue = IOC.resolve(Keys.getOrAdd(IQueue.class.getCanonicalName()));

            int count = 0;
            for (IFeature feature : features) {
                this.featuresInProgress.put(feature.getId(), feature);
                IMessageProcessingSequence processingSequence = IOC.resolve(
                        Keys.getOrAdd(MESSAGE_PROCESSOR_SEQUENCE_FACTORY_STRATEGY_NAME), stackDepth, scatterChain
                );
                IMessageProcessor messageProcessor = IOC.resolve(
                        Keys.getOrAdd(MESSAGE_PROCESSOR_FACTORY_STRATEGY_NAME), queue, processingSequence
                );
                IObject message = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME));
                message.setValue(this.featureFN, feature);
                message.setValue(this.afterFeaturesCallbackQueueFN, afterFeaturesCallbackQueue);
                IObject context = IOC.resolve(Keys.getOrAdd(IOBJECT_FACTORY_STRATEGY_NAME));
                messageProcessor.process(message, context);
                ++count;
            }
            if (count > 0) {
                mp.pauseProcess();
                this.requestProcesses.put(mp, features);
                this.requestProcessesForInfo.put(mp, featuresForInfo);
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
     *
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void onFeatureLoaded(final OnFeatureLoadedWrapper wrapper)
            throws FeatureManagementException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
            System.out.println("[INFO] " + Thread.currentThread().getName() +
                    " onFeatureLoaded started on feature " + feature.getDisplayName());

            this.featuresInProgress.remove(feature.getId());
            if (feature.isFailed()) {
                this.failedFeatures.put(feature.getId(), feature);

            } else {
                this.loadedFeatures.put(feature.getId(), feature);
                for (IFeature processingFeature : this.featuresInProgress.values()) {
                    removeLoadedFeaturesFromFeatureDependencies(processingFeature);
                }
                checkAndRunConnectedFeatures();
            }

            Collection<IMessageProcessor> processesToContinue = new HashSet<>();
            this.requestProcesses.forEach((k, v) -> {
                v.remove(feature);
                if (v.isEmpty()) {
                    processesToContinue.add(k);
                    this.requestProcesses.remove(k);
                }
            });
            processesToContinue.forEach((mp) -> {
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
                                    this.requestProcessesForInfo.get(mp).stream().map(
                                            a -> "\n" + a.getDisplayName() + " - " + (!a.isFailed() ? "(OK)" : "(Failed)")
                                    ).collect(Collectors.toList())
                    );
                    System.out.println("[INFO] elapsed time - " + elapsedTimeToLocalTime.format(df) + ".");
                    System.out.println("\n\n");
                    this.requestProcessesForInfo.remove(mp);
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
        System.out.println("[INFO] " + Thread.currentThread().getName() +
                " onFeatureLoaded finished on feature " + feature.getDisplayName());
    }

    /**
     * Ends step of feature processing
     *
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void onFeatureStepCompleted(final OnFeatureStepCompletedWrapper wrapper)
            throws FeatureManagementException {
        IFeature feature;
        try {
            feature = wrapper.getFeature();
            System.out.println("[INFO] " + Thread.currentThread().getName() +
                    " onFeatureStepCompleted started on feature " + feature.getDisplayName());
            checkAndRunConnectedFeatures();
            Set<String> featureDependencies = feature.getDependencies();

            if (null != featureDependencies) {
                VersionManager.addItem(
                        feature.getId(),
                        getEmptyIfNull(feature.getGroupId()) + FEATURE_NAME_DELIMITER + getEmptyIfNull(feature.getName()),
                        getEmptyIfNull(feature.getVersion())
                );

                removeLoadedFeaturesFromFeatureDependencies(feature);

                if (featureDependencies.isEmpty()) {
                    VersionManager.finalizeItemDependencies(feature.getId(), VersionManager.getCoreId());
                } else {
                    IMessageProcessor mp = wrapper.getMessageProcessor();
                    mp.pauseProcess();
                    this.featureProcesses.put(mp, feature);
                }
            }
        } catch (InvalidArgumentException | ReadValueException | AsynchronousOperationException e) {
            throw new FeatureManagementException(e);
        }
        System.out.println("[INFO] " + Thread.currentThread().getName() +
                " onFeatureStepCompleted finished on feature " + feature.getDisplayName());
    }

    /**
     * Gets state of added features
     *
     * @param wrapper the wrapped message for getting needed data and storing result
     * @throws FeatureManagementException if any errors occurred on feature processing
     */
    public void getState(final FeatureManagerStateWrapper wrapper)
            throws FeatureManagementException {
        try {
            wrapper.setLoadedFeatures(this.loadedFeatures.values());
            wrapper.setFailedFeatures(this.failedFeatures.values());
            wrapper.setProcessingFeatures(this.featuresInProgress.values());
            wrapper.setFrozenFeatureProcesses(this.featureProcesses);
            wrapper.setFrozenRequests(this.requestProcesses);
        } catch (ChangeValueException e) {
            throw new FeatureManagementException("Could not set parameter to IObject.", e);
        }
    }

    private void checkAndRunConnectedFeatures() {
        Collection<IMessageProcessor> needContinueFeatures = new HashSet<>();
        this.featureProcesses.forEach((k, v) -> {
            if (v.getDependencies().isEmpty()) {
                needContinueFeatures.add(k);
                this.featureProcesses.remove(k);
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

    private void removeLoadedFeaturesFromFeatureDependencies(final IFeature feature)
            throws FeatureManagementException {

        Set<String> featureDependencies = feature.getDependencies();
        if (null != featureDependencies) {
            for (Iterator<String> iterator = featureDependencies.iterator(); iterator.hasNext(); ) {

                String dependency = iterator.next();

                IFeature baseFeature = null;
                for (IFeature loadedFeature : this.loadedFeatures.values()) {
                    if (matchDependency(dependency, loadedFeature)) {
                        if (baseFeature == null || loadedFeature.getVersion().compareTo(baseFeature.getVersion()) >= 0) {
                            baseFeature = loadedFeature;
                        }
                    }
                }
                if (null != baseFeature) {
                    VersionManager.addItemDependency(feature.getId(), baseFeature.getId());
                    iterator.remove();
                    System.out.println("[INFO] Dependency '" + dependency + "' was removed from feature '" + feature.getDisplayName() + "'. Loaded feature:'" + baseFeature.getDisplayName() + "'.");
                }
            }
        }
    }

    private void checkUnresolved()
            throws FeatureManagementException {
        int minDependencies = this.featuresInProgress
                .values()
                .stream()
                .filter(feature -> null != feature.getDependencies())
                .map(f -> f.getDependencies().size())
                .min(Integer::compareTo)
                .orElse(0);
        if (
                !this.featuresInProgress.isEmpty()
                        && minDependencies > 0
        ) {
            Set<String> unresolved = this.featuresInProgress
                    .values()
                    .stream()
                    .map(IFeature::getDependencies)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            Iterator<String> iterator = unresolved.iterator();
            while (iterator.hasNext()) {
                String dependency = iterator.next();

                for (IFeature feature : this.featuresInProgress.values()) {
                    if (matchDependency(dependency, feature)) {
                        iterator.remove();
                    }
                }
            }

            System.out.println("[INFO] The server waits for the following features to continue: " +
                    unresolved.stream().map(a -> "\n\t\t" + a).collect(Collectors.toList())
            );
        }
    }

    private String getEmptyIfNull(String s) {
        return (s == null ? "" : s);
    }

    private boolean matchDependency(String dependencyName, IFeature feature)
            throws FeatureManagementException {
        String[] dependency = parseFullName(dependencyName);
        return (feature.getGroupId().equals(dependency[0]) &&
                feature.getName().equals(dependency[1]) &&
                feature.getVersion().compareTo(dependency[2]) >= 0);
    }

    // todo: replace this code by parsing strategy
    private String[] parseFullName(String fullName)
            throws FeatureManagementException {
        String[] dependencyNames = fullName.split(FEATURE_NAME_DELIMITER);
        if (dependencyNames.length < 2) {
            throw new FeatureManagementException("Wrong feature name or dependency format '"+fullName+"'.");
        }
        String[] result = {
                dependencyNames[0],
                dependencyNames[1],
                dependencyNames.length > 2 ? dependencyNames[2] : ""
        };
        return result;
    }
}
