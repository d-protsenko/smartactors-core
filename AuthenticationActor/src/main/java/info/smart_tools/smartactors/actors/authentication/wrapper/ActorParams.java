package info.smart_tools.smartactors.actors.authentication.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

/**
 * Wrapper for actor params
 */
public interface ActorParams {
    /**
     * @return field name of validated value
     * @throws ReadValueException
     */
    String getUserAgentFieldName() throws ReadValueException;
}
