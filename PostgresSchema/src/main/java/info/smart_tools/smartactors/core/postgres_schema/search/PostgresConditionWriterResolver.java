package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;

import java.util.HashMap;
import java.util.Map;

public abstract class PostgresConditionWriterResolver implements ConditionWriterResolver {

    private Map<String, ConditionWriter> operatorWriters = new HashMap<>();
    private final String defaultCompositionOperator = "$and";

    public void addOperator(final String name, final ConditionWriter writer) {
        operatorWriters.put(name, writer);
    }

    /**
     * Resolves valid field path for psql db.
     *
     * @param name - name of field path for psql db.
     *
     * @return a <pre>FieldPath</pre> object with valid field path for psql db.
     *
     * @throws QueryBuildException when invalid name of field path for psql db.
     */
    public FieldPath resolveFieldName(final String name) throws QueryBuildException {
        return PostgresFieldPath.fromString(name);
    }

    public ConditionWriter resolve(final String name) throws QueryBuildException {
        if (name == null) {
            return this.resolve(defaultCompositionOperator);
        }

        ConditionWriter operatorWriter = operatorWriters.get(name);

        if (operatorWriter != null) {
            return operatorWriter;
        }

        return resolveFieldWriter(name);
    }

    protected ConditionWriter resolveFieldWriter(final String fieldName)
        throws QueryBuildException {
        final FieldPath path = this.resolveFieldName(fieldName);

        return (query, resolver, contextFieldName, queryParameter, setters) -> {
            if (contextFieldName != null) {
                throw new QueryBuildException("Field names not allowed inside of field context.");
            }

            this.resolve(null).write(query, resolver, path, queryParameter, setters);
        };
    }

    {
        addOperator("$and", Conditions::writeAndCondition);
        addOperator("$or", Conditions::writeOrCondition);
        addOperator("$not", Conditions::writeNotCondition);
    }
}
