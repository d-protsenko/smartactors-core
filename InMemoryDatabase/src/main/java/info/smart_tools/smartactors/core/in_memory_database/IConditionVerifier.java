package info.smart_tools.smartactors.core.in_memory_database;

import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

/**
 * Class, that should verify, is IObject satisfy condition
 */
public interface IConditionVerifier {

    /**
     * Method for verify is
     *
     * @param condition condition, that should be verified
     * @param document  document, that should be verified
     */
    boolean verify(final IObject condition, final IObject document);
}
