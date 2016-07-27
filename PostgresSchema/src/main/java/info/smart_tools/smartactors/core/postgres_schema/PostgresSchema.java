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
public final class PostgresSchema {

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
     * Fills the statement body with the sequence name for the collection for 'nextval' query
     * to select the next document ID from the database.
     * @param statement statement to fill the body
     * @param collection collection name to use to construct the sequence name
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void nextId(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("SELECT nextval('");
            writer.write(collection.toString());
            writer.write("_");
            writer.write(ID_COLUMN);
            writer.write("_seq') AS id");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build nextId body", e);
        }
    }

    /**
     * Fills the statement body with the collection name for the INSERT statement.
     * @param statement statement to fill the body
     * @param collection collection name to use as the table name
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void insert(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("INSERT INTO ");
            writer.write(collection.toString());
            writer.write(" (");
            writer.write(ID_COLUMN);
            writer.write(", ");
            writer.write(DOCUMENT_COLUMN);
            writer.write(") VALUES (?, ?::jsonb)");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build insert body", e);
        }
    }

    /**
     * Fills the statement body with the collection name for the UPDATE statement
     * @param statement statement to fill the body
     * @param collection collection name to use as the table name
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void update(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("UPDATE ");
            writer.write(collection.toString());
            writer.write(" AS tab ");
            writer.write("SET ");
            writer.write(DOCUMENT_COLUMN);
            writer.write(" = docs.document FROM (VALUES (?, ?::jsonb)) AS docs (id, document) WHERE tab.");
            writer.write(ID_COLUMN);
            writer.write(" = docs.id");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build update body", e);
        }
    }

    /**
     * Fills the statement body with the collection name for the SELECT statement to find the document by ID.
     * @param statement statement to fill the body
     * @param collection collection name to use as the table name
     */
    public static void getById(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            writer.write("SELECT ");
            writer.write(DOCUMENT_COLUMN);
            writer.write(" FROM ");
            writer.write(collection.toString());
            writer.write(" WHERE ");
            writer.write(ID_COLUMN);
            writer.write(" = ?");
        } catch (IOException e) {
            throw new QueryBuildException("Failed to build getById body", e);
        }
    }

}
