package info.smart_tools.smartactors.feature_management.download_feature_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.Set;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface DownloadFeatureWrapper {

    IFeature getFeature()
            throws ReadValueException;
}
