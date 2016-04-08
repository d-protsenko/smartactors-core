package info.smart_tools.smartactors.core.recursive_scope;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link info.smart_tools.smartactors.core.iscope.IScope}
 * with recursive call method getValue of parent scope
 */
public class Scope implements IScope {

    private Map<Object, Object> scopeStorage = new HashMap<Object, Object>();
    private Scope parent;

    /**
     * Constructs new Scope with defined parent scope
     * @param parent the parent scope.
     */
    public Scope(final Scope parent) {
        this.parent = parent;
    }

    /**
     * Get a value by the key from the scope.
     * @param key given key
     * @return the found value
     * @throws ScopeException if value is not found or error was occurred
     */
    public Object getValue(final Object key)
            throws ScopeException {
        Object value = scopeStorage.get(key);

        if (value == null) {
            try {
                return parent.getValue(key);
            } catch (NullPointerException e) {
                throw new ScopeException("Value not found", e);
            }
        }
        return value;
    }

    /**
     * Stores a value to the scope associating it with defined key
     * @param key given key
     * @param value given value
     * @throws ScopeException if error was occurred
     */
    public void setValue(final Object key, final Object value)
            throws ScopeException {
        scopeStorage.put(key, value);
    }

    /**
     * Removes the value associated with key if any
     * @param key given key
     * @throws ScopeException if value is not found or error was occurred
     */
    public void deleteValue(final Object key)
            throws ScopeException {
        scopeStorage.remove(key);
    }
}
