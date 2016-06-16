package info.smart_tools.smartactors.core.examples.scope;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import info.smart_tools.smartactors.core.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.core.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  A sample of scope based IoC.
 */
public class MyIOC {

    /** Key for getting instance of storage from current scope */
    private static IKey STORAGE_KEY;

    static {
        try {
            STORAGE_KEY = new Key(java.util.UUID.randomUUID().toString());
            ScopeProvider.subscribeOnCreationNewScope(
                    newScope -> {
                        try {
                            Map<IKey, Object> currentStorage = Collections.emptyMap();
                            try {
                                IScope currentScope = ScopeProvider.getCurrentScope();
                                currentStorage = (Map<IKey, Object>) currentScope.getValue(STORAGE_KEY);
                            } catch (ScopeException | ScopeProviderException e) {
                                // current storage does not exists, create an empty one
                            }
                            newScope.setValue(STORAGE_KEY, new ConcurrentHashMap<IKey, Object>(currentStorage));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (InvalidArgumentException | ScopeProviderException e) {
            throw new RuntimeException(e);
        }
    }

    private MyIOC() {
        // avoid instantiation
    }

    public static void init() {
        // empty method to force initialization of the class when necessary
    }

    public static void register(final IKey key, final Object value) throws MyIOCException {
        try {
            Map<IKey, Object> storage = (Map<IKey, Object>) ScopeProvider.getCurrentScope().getValue(STORAGE_KEY);
            storage.put(key, value);
        } catch (Throwable e) {
            throw new MyIOCException(e);
        }
    }

    public static <T> T resolve(final IKey<T> key) throws MyIOCException {
        try {
            Map<IKey, Object> storage = (Map<IKey, Object>) ScopeProvider.getCurrentScope().getValue(STORAGE_KEY);
            return (T) storage.get(key);
        } catch (Throwable e) {
            throw new MyIOCException(e);
        }
    }

}
