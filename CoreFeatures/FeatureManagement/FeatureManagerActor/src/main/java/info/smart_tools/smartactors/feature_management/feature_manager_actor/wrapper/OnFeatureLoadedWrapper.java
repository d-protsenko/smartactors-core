package info.smart_tools.smartactors.feature_management.feature_manager_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Interface of wrapper for {@link info.smart_tools.smartactors.feature_management.feature_manager_actor.FeatureManagerActor}
 */
public interface OnFeatureLoadedWrapper {

    /**
     * Gets feature from the message
     * @return the feature
     * @throws ReadValueException if any errors occurred on wrapper reading
     */
    IFeature getFeature()
            throws ReadValueException;
}
