package info.smart_tools.smartactors.core.db_task.search.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.sql_commons.ConditionsResolverBase;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;

/**
 *
 */
final class ConditionsWriterResolver extends ConditionsResolverBase {

    private ConditionsWriterResolver() {
        Operators.addAll(this);
    }

    /**
     *
     * @return
     */
    public static ConditionsWriterResolver create() {
        return new ConditionsWriterResolver();
    }

    /**
     *
     * @param name
     * @return
     * @throws QueryBuildException
     */
    public FieldPath resolveFieldName(final String name) throws QueryBuildException {
        return PSQLFieldPath.fromString(name);
    }
}
