package info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface FeatureManagerStateWrapper {

    void setLoadedFeatures(Collection<IFeature> loadedFeatures)
            throws ChangeValueException;

    void setFailedFeatures(Collection<IFeature> failedFeatures)
            throws ChangeValueException;

    void setProcessingFeatures(Collection<IFeature> processingFeature)
            throws ChangeValueException;

    void setFrozenFeatureProcesses(Map<IMessageProcessor, IFeature> frozenFeature)
            throws ChangeValueException;

    void setFrozenRequests(Map<IMessageProcessor, Set<IFeature>> frozenRequest)
            throws ChangeValueException;
}
