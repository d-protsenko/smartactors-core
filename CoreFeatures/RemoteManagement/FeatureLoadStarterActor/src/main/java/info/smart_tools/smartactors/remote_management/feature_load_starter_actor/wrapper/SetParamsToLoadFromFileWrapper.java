package info.smart_tools.smartactors.remote_management.feature_load_starter_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

public interface SetParamsToLoadFromFileWrapper {

    /**
     * Set the name of feature file to use in onNewFile chain
     * @throws ChangeValueException
     */
    void setFileName(final String fileName)
            throws ChangeValueException;

    /**
     * Set the directory to use in onNewFile chain
     * @throws ChangeValueException
     */
    void setObservedDirectory(final String directory)
            throws ChangeValueException;

    /**
     * Get feature location for loading
     * @return feature location
     * @throws ReadValueException
     */
    String getFeatureLocation()
            throws ReadValueException;

    /**
     * Get destination server directory to store loading feature
     * @return destination feature directory on server
     * @throws ReadValueException
     */
    String getFeatureDestinationPath()
            throws ReadValueException;
}
