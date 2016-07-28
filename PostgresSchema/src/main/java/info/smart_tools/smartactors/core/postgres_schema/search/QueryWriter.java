package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_connection.SQLQueryParameterSetter;

import java.util.List;

/**
 * Writes a part of SQL statement for the search condition.
 */
@FunctionalInterface
public interface QueryWriter {

    /**
     * Writes the part of the SQL statement body
     * @param query query to which body to write the condition
     * @param resolver the resolver to resolve nested conditions
     * @param contextFieldPath current document field path
     * @param queryParameter currently processing query parameter
     * @param setters the list of parameter setters to be appended
     * @throws QueryBuildException when something goes wrong
     */
    void write(QueryStatement query, QueryWriterResolver resolver, FieldPath contextFieldPath,
               Object queryParameter, List<SQLQueryParameterSetter> setters) throws QueryBuildException;

}
