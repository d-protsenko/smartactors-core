package info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper;

import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.Collection;
import java.util.List;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface CreateFeaturesWrapper {

    void setFeatures(Collection<IFeature> features)
            throws ChangeValueException;

    List<IObject> getFeaturesDescription()
            throws ReadValueException;

    List<IObject> getRepositoriesDescription()
            throws ReadValueException;

    String getFeatureDirectory()
            throws ReadValueException;
}
