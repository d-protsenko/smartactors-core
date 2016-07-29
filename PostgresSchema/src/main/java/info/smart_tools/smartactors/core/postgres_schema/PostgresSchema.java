package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.search.PostgresQueryWriterResolver;

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
    public static final String ID_COLUMN = "id";
    /**
     * Name of the DOCUMENT column.
     */
    public static final String DOCUMENT_COLUMN = "document";

    /**
     * Dictionary for Full Text Search
     * TODO: don't hardcode Russian
     */
    public static final String FTS_DICTIONARY = "russian";

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
     * @throws QueryBuildException if the statement body cannot be built
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

    /**
     * Fills the statement body and it's list of parameter setters with the collection name and WHERE clause
     * for the SELECT statement to search the document by it's fields.
     * <p>
     *     The example of search criteria.
     *     <pre>
     *  {
     *      "filter": {
     *          "$or": [
     *              "a": { "$eq": "b" },
     *              "b": { "$gt": 42 }
 *              ]
     *      }
     *  }
     *     </pre>
     * </p>
     * @param statement statement to fill the body and add parameter setters
     * @param collection collection name to use as the table name
     * @param criteria complex JSON describing the search criteria
     * @throws QueryBuildException when something goes wrong
     */
    public static void search(final QueryStatement statement, final CollectionName collection, final IObject criteria)
            throws QueryBuildException {
        Writer writer = statement.getBodyWriter();
        try {
            IFieldName filterField = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "filter");
            IObject filter = (IObject) criteria.getValue(filterField);
            PostgresQueryWriterResolver resolver = new PostgresQueryWriterResolver();
            writer.write("SELECT ");
            writer.write(DOCUMENT_COLUMN);
            writer.write(" FROM ");
            writer.write(collection.toString());
            writer.write(" WHERE ");
            resolver.resolve(null).write(statement, resolver, null, filter);
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build search query", e);
        }
    }

}
