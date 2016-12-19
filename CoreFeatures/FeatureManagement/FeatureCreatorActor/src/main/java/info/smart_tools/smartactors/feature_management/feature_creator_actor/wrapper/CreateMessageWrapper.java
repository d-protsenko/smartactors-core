package info.smart_tools.smartactors.feature_management.feature_creator_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

import java.util.List;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface CreateMessageWrapper {

    String getFileName()
            throws ReadValueException;

    String getObservedDirectory()
            throws ReadValueException;

    void setJsonFeaturesDescription(List<IObject> features)
            throws ChangeValueException;

    void setJsonRepositoriesDescription(List<IObject> repositories)
            throws ChangeValueException;

}
