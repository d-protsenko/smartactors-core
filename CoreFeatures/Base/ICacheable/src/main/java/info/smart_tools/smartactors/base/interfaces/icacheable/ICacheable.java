package info.smart_tools.smartactors.base.interfaces.icacheable;

import info.smart_tools.smartactors.base.interfaces.icacheable.exception.CacheDropException;

public interface ICacheable {

    void dropCache()
            throws CacheDropException;

    void dropCacheFor(final Object key)
            throws CacheDropException;
}
