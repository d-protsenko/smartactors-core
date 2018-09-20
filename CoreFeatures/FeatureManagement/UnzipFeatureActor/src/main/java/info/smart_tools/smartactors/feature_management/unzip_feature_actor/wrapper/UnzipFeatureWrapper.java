package info.smart_tools.smartactors.feature_management.unzip_feature_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Interface of wrapper for {@link info.smart_tools.smartactors.feature_management.unzip_feature_actor.UnzipFeatureActor}
 */
public interface UnzipFeatureWrapper {

    /**
     * Gets feature from the message
     * @return the instance of {@link IFeature}
     * @throws ReadValueException if any errors occurred on wrapper reading
     */
    IFeature getFeature()
            throws ReadValueException;

    /**
     * Puts the feature to message for further processing
     * @param feature the feature to put into message
     * @throws ChangeValueException if any errors occurred on writing to the wrapper
     */
    void setFeature(IFeature feature)
            throws ChangeValueException;
}
