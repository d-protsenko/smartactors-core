package info.smart_tools.smartactors.base.interfaces.icacheable;

import info.smart_tools.smartactors.base.interfaces.icacheable.exception.DropCacheException;

public interface ICacheable {

    void dropCache()
            throws DropCacheException;

    void dropCacheFor(final Object key)
            throws DropCacheException;
}
