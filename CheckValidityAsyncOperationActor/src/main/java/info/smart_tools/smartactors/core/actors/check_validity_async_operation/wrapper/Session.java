package info.smart_tools.smartactors.core.actors.check_validity_async_operation.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;

import java.util.List;

/**
 * Wrapper for session
 */
public interface Session {
    /**
     * Returns the list with all identifiers of asynchronous operations which are admissible for this session
     * @return List with identifiers
     * @throws ChangeValueException
     * @throws ReadValueException
     */
    List<String> getIdentifiers() throws ChangeValueException, ReadValueException;
}
