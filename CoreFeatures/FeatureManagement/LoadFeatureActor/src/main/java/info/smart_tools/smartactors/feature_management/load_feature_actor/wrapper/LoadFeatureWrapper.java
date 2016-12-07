package info.smart_tools.smartactors.feature_management.load_feature_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface LoadFeatureWrapper {

    IFeature getFeature()
            throws ReadValueException;
}
