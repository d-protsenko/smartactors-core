package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;

import java.io.IOException;
import java.io.Writer;

/**
 * A set of static methods to take statements to be executed in the Postgres database.
 * The statements are to perform operations over jsonb documents.
 */
public class PostgresSchema {

    /**
     * Name of the ID column.
     */
    private static final String ID_COLUMN = "id";
    /**
     * Name of the DOCUMENT column.
     */
    private static final String DOCUMENT_COLUMN = "document";

    /**
     * Private constructor to avoid instantiation.
     */
    private PostgresSchema() {
    }

    /**
     * Fills the statement body with the collection name for the INSERT statement.
     * @param statement statement to fill the body
     * @param collection collection name to use as the table name
     * @throws QueryBuildException if the document body cannot be built
     */
    public static void insert(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("INSERT INTO ");
            writer.write(collection.toString());
            writer.write(" (");
            writer.write(DOCUMENT_COLUMN);
            writer.write(") VALUES (?::jsonb) ");
            writer.write("RETURNING ");
            writer.write(ID_COLUMN);
            writer.write(" AS id");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build query body", e);
        }
    }

    /**
     * Fills the statement body with the collection name for the UPDATE statement
     * @param statement statement to fill the body
     * @param collection collection name to use as the table name
     * @throws QueryBuildException if the document body cannot be built
     */
    public static void update(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("UPDATE ");
            writer.write(collection.toString());
            writer.write(" AS tab ");
            writer.write("SET tab.");
            writer.write(DOCUMENT_COLUMN);
            writer.write(" = docs.document FROM (VALUES (?, ?::jsonb)) AS docs (id, document) WHERE tab.");
            writer.write(ID_COLUMN);
            writer.write(" = docs.id");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build query body", e);
        }
    }

}
