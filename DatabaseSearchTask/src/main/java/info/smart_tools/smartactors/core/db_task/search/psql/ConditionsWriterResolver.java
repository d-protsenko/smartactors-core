package info.smart_tools.smartactors.core.db_task.search.psql;


import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.sql_commons.ConditionsResolverBase;
import info.smart_tools.smartactors.core.sql_commons.FieldPath;

final class ConditionsWriterResolver extends ConditionsResolverBase {
    {
        Operators.addAll(this);
    }

    public FieldPath resolveFieldName(final String name) throws QueryBuildException {
        return PSQLFieldPath.fromString(name);
    }
}
