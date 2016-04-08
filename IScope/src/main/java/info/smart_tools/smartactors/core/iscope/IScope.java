package info.smart_tools.smartactors.core.iscope;

import info.smart_tools.smartactors.core.iscope.exception.ScopeException;

/**
 * Scope interface
 */
public interface IScope {

    /**
     * Get a value by the key from the scope
     * @param key given key
     * @return found object
     * @throws ScopeException if value is not found or error has been occurred
     */
    Object getValue(final Object key)
            throws ScopeException;

    /**
     * Stores an value in scope associating it with defined key
     * @param key given key
     * @param value given value
     * @throws ScopeException if error has been occurred
     */
    void setValue(Object key, Object value)
            throws ScopeException;

    /**
     * Removes the value associated with {@code key} if any
     * @param key given key
     * @throws ScopeException if value is absent or other error has been occurred
     */
    void deleteValue(Object key)
            throws ScopeException;
}
