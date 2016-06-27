package info.smart_tools.smartactors.core.sql_commons;

import info.smart_tools.smartactors.core.ikey.IKey;

import java.util.*;

/**
 * Utility class for IOC-strategy for resolving compiled query by connection and query type
 */
final public class QueryKey implements IKey {
    final private String connectionId;
    final private String queryType;
    final private String collection;
    final private List<?> options;

    private QueryKey(
            final String connectionId,
            final String queryType,
            final String collection,
            final List<?> options
    ) {
        this.connectionId = connectionId;
        this.queryType = queryType;
        this.collection = collection;
        this.options = new LinkedList<>(options);
    }

    public static QueryKey create(
            final String connectionId,
            final String taskName,
            final String collection,
            final List<?> options
    ) {
        return new QueryKey(connectionId, taskName, collection, options);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryKey queryKey = (QueryKey) o;

        return connectionId.equals(queryKey.connectionId) && queryType.equals(queryKey.queryType) &&
                collection.equals(queryKey.collection) && options.equals(queryKey.options);
    }

    @Override
    public int hashCode() {
        return  31 * connectionId.hashCode() + queryType.hashCode() +
                collection.hashCode() + options.hashCode();
    }
}
