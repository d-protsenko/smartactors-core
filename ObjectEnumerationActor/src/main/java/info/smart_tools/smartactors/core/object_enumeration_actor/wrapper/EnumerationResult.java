package info.smart_tools.smartactors.core.object_enumeration_actor.wrapper;

import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;

import java.util.List;

/**
 *
 */
public interface EnumerationResult {
    /**
     * Store result of enumeration.
     *
     * @param items    list of identifiers of enumerated objects
     * @throws ChangeValueException if errors occurs writing the value
     */
    void setItems(final List items) throws ChangeValueException;
}
