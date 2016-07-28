package info.smart_tools.smartactors.core.postgres_schema.search;


import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A set of methods to correctly write down select conditions like AND or OR in the SQL queries.
 */
public final class Conditions {

    /**
     * Private constructor to avoid instantiation.
     */
    private Conditions(){
    }

    /**
     * Writes the AND condition.
     * @param query the query into which body to write the condition
     * @param resolver a resolver which able to return correct {@link ConditionWriter}
     * @param contextFieldPath current path to object field
     * @param queryParameter current query parameter value, the argument to the condition
     * @param setters a list of parameter setters to be added to the query, is filled by the call
     * @throws QueryBuildException if the query cannot be built
     */
    public static void writeAndCondition(
            final QueryStatement query,
            final ConditionWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter,
            final List<SQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        writeCompositeCondition("(", ")", "AND", query, resolver, contextFieldPath, queryParameter, setters);
    }

    /**
     * Writes the OR condition.
     * @param query the query into which body to write the condition
     * @param resolver a resolver which able to return correct {@link ConditionWriter}
     * @param contextFieldPath current path to object field
     * @param queryParameter current query parameter value, the argument to the condition
     * @param setters a list of parameter setters to be added to the query, is filled by the call
     * @throws QueryBuildException if the query cannot be built
     */
    public static void writeOrCondition(
            final QueryStatement query,
            final ConditionWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter,
            final List<SQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        writeCompositeCondition("(", ")", "OR", query, resolver, contextFieldPath, queryParameter, setters);
    }

    /**
     * Writes the NOT condition
     * @param query the query into which body to write the condition
     * @param resolver a resolver which able to return correct {@link ConditionWriter}
     * @param contextFieldPath current path to object field
     * @param queryParameter current query parameter value, the argument to the condition
     * @param setters a list of parameter setters to be added to the query, is filled by the call
     * @throws QueryBuildException if the query cannot be built
     */
    public static void writeNotCondition(
            final QueryStatement query,
            final ConditionWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter,
            final List<SQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        writeCompositeCondition("(NOT(", "))", "AND", query, resolver, contextFieldPath, queryParameter, setters);
    }

    /**
     * Writes the default (true) condition to the query
     * @param query query where to write the condition
     * @throws IOException if write to the query body failed
     */
    private static void writeDefaultEmptyCondition(final QueryStatement query)
        throws IOException {
        query.getBodyWriter().write("(true)");
    }

    /**
     * Writes some composite condition to the query.
     * @param prefix prefix to prepend the whole condition, typically a bracket (
     * @param postfix postfix to postpone the whole condition, typically a bracket )
     * @param delimiter characters to separate condition elements, for example "AND"
     * @param query the query into which body to write the condition
     * @param resolver a resolver which able to return correct {@link ConditionWriter}
     * @param contextFieldPath current path to object field
     * @param queryParameter current query parameter value, the argument to the condition
     * @param setters a list of parameter setters to be added to the query, is filled by the call
     * @throws QueryBuildException
     */
    private static void writeCompositeCondition(
            final String prefix,
            final String postfix,
            final String delimiter,
            final QueryStatement query,
            final ConditionWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter,
            final List<SQLQueryParameterSetter> setters
    ) throws QueryBuildException {
        Writer writer = query.getBodyWriter();

        try {
            if (queryParameter instanceof IObject) {
                Iterator<Map.Entry<IFieldName, Object>> paramIterator = ((IObject) queryParameter).iterator();

                if (!paramIterator.hasNext()) {
                    writeDefaultEmptyCondition(query);
                    return;
                }

                writer.write(prefix);

                while (paramIterator.hasNext()) {
                    Map.Entry<IFieldName, Object> entry = paramIterator.next();
                    String key = String.valueOf(entry.getKey());
                    resolver.resolve(key).write(query, resolver, contextFieldPath, entry.getValue(), setters);
                    if (paramIterator.hasNext()) {
                        writer.write(delimiter);
                    }
                }

                writer.write(postfix);

            } else if (queryParameter instanceof List) {
                List<?> paramAsList = (List<?>) queryParameter;

                if (paramAsList.size() == 0) {
                    writeDefaultEmptyCondition(query);
                    return;
                }

                writer.write(prefix);

                ConditionWriter resolved = resolver.resolve(null);

                Iterator<?> iterator = paramAsList.iterator();
                while (iterator.hasNext()) {
                    Object entry = iterator.next();
                    resolved.write(query, resolver, contextFieldPath, entry, setters);
                    if (iterator.hasNext()) {
                        writer.write(delimiter);
                    }
                }

                writer.write(postfix);

            } else {
                throw new QueryBuildException("Composite node value should be an object or an array");
            }
        } catch (IOException e) {
            throw new QueryBuildException("Error while writing a query string", e);
        }
    }
}
