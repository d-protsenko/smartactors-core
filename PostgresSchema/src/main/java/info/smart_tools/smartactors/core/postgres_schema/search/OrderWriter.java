package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;

import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Writes the ORDER clause into request to Postgres database.
 */
public class OrderWriter {

    /**
     * Writes an ORDER clause into the query statement.
     * @param queryStatement - query where to write ORDER clause and add parameters.
     * @param sortMessage - message describing how to order
     * @throws QueryBuildException when something goes wrong
     */
    public void write(QueryStatement queryStatement, IObject sortMessage) throws QueryBuildException {
        try {
            Writer writer = queryStatement.getBodyWriter();
            writer.write("ORDER BY");

            Iterator<Map.Entry<IFieldName, Object>> sortEntries = sortMessage.iterator();
            while (sortEntries.hasNext()) {
                Map.Entry<IFieldName, Object> entry = sortEntries.next();
                FieldPath fieldPath = PostgresFieldPath.fromString(String.valueOf(entry.getKey()));     // TODO: convert using IOC
                String sortDirection = resolveSortDirection(entry.getValue());      // TODO: convert using IOC
                writer.write("(");
                writer.write(fieldPath.toSQL());
                writer.write(")");
                writer.write(sortDirection);
                if (sortEntries.hasNext()) {
                    writer.write(",");
                }
            }
        } catch (QueryBuildException qbe) {
            throw qbe;
        } catch (Exception e) {
            throw new QueryBuildException("Error while writing ORDER BY clause", e);
        }
    }

    private String resolveSortDirection(Object direction) throws QueryBuildException {
        if ("asc".equalsIgnoreCase(String.valueOf(direction))) {
            return "ASC";
        } else if ("desc".equalsIgnoreCase(String.valueOf(direction))) {
            return "DESC";
        }
        throw new QueryBuildException("Invalid direction value: " + direction);
    }

}
