package info.smart_tools.smartactors.feature_management.directory_watcher_actor.wrapper;

import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;

/**
 * Created by sevenbits on 12/5/16.
 */
public interface StartWatchingWrapper {

    String getObservedDirectory()
            throws ReadValueException;

    String getExecutionChain()
            throws ReadValueException;

    String getFileNameFieldName()
            throws ReadValueException;

    String getObservedDirectoryFieldName()
            throws ReadValueException;
}
