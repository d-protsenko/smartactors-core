package info.smart_tools.smartactors.actor.change_password.wrapper;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for constructor of {@link info.smart_tools.smartactors.actor.change_password.ChangePasswordActor}
 */
public interface ChangePasswordConfig {

    /**
     * Getter
     * @return wrapped collection name
     * @throws ReadValueException if error during get is occurred
     */
    CollectionName getCollectionName() throws ReadValueException;
}
