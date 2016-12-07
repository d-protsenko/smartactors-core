package info.smart_tools.smartactors.feature_management.all_in_direcory_feature_tracker.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;

import java.util.Collection;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface FeatureTrackerWrapper {

    String getPath()
            throws ReadValueException;

    String getExecutionChain()
            throws ReadValueException;

    IMessageProcessor getMessageProcessor()
            throws ReadValueException;

    void setFeatures(Collection<IFeature> features)
            throws ChangeValueException;
}
