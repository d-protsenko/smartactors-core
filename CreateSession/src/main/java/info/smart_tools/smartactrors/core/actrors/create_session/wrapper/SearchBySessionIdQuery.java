package info.smart_tools.smartactrors.core.actrors.create_session.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

/**
 * Wrapper for CreateSessionActor
 */
public interface SearchBySessionIdQuery {

    /**
     * Set collection name into wrapper
     * @param collectionName
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    void setCollectionName(String collectionName) throws ChangeValueException, ReadValueException;

    /**
     *
     * @param id
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    void setId(String id) throws ChangeValueException, ReadValueException;
}
