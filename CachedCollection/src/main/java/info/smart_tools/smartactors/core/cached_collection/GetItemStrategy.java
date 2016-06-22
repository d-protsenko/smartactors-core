package info.smart_tools.smartactors.core.cached_collection;

import info.smart_tools.smartactors.core.iobject.IObject;

import java.util.List;

@FunctionalInterface
public interface GetItemStrategy {

    IObject getItem(List<IObject> items);
}
