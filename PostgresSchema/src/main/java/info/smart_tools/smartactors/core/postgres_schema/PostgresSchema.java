package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.indexes.IndexCreators;

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
     * Fills the statement body with the CREATE TABLE sentence and CREATE INDEX sentences
     * to create the desired collection and it's indexes.
     * @param statement statement to fill the body
     * @param collection collection name to use to construct the sequence name
     * @param options document describing a set of options for the collection creation
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void create(final QueryStatement statement, final CollectionName collection, IObject options) throws QueryBuildException {
        try {
            Writer body = statement.getBodyWriter();
            body.write("CREATE TABLE ");
            body.write(collection.toString ());
            body.write(" (");
            body.write(ID_COLUMN);
            body.write(" bigserial PRIMARY KEY, ");
            body.write(DOCUMENT_COLUMN);
            body.write(" jsonb NOT NULL);\n");
            if (options != null) {
                IndexCreators.writeIndexes(body, collection, options);
            }
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build create body", e);
        }
    }

    /**
     * Fills the statement body with the sequence name for the collection for 'nextval' query
     * to select the next document ID from the database.
     * @param statement statement to fill the body
     * @param collection collection name to use to construct the sequence name
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void nextId(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        try {
            Writer body = statement.getBodyWriter();
            body.write("SELECT nextval('");
            body.write(collection.toString());
            body.write("_");
            body.write(ID_COLUMN);
            body.write("_seq') AS id");
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
        try {
            Writer body = statement.getBodyWriter();
            body.write("INSERT INTO ");
            body.write(collection.toString());
            body.write(" (");
            body.write(ID_COLUMN);
            body.write(", ");
            body.write(DOCUMENT_COLUMN);
            body.write(") VALUES (?, ?::jsonb)");
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
        try {
            Writer body = statement.getBodyWriter();
            body.write("UPDATE ");
            body.write(collection.toString());
            body.write(" AS tab ");
            body.write("SET ");
            body.write(DOCUMENT_COLUMN);
            body.write(" = docs.document FROM (VALUES (?, ?::jsonb)) AS docs (id, document) WHERE tab.");
            body.write(ID_COLUMN);
            body.write(" = docs.id");
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
        try {
            Writer body = statement.getBodyWriter();
            body.write("SELECT ");
            body.write(DOCUMENT_COLUMN);
            body.write(" FROM ");
            body.write(collection.toString());
            body.write(" WHERE ");
            body.write(ID_COLUMN);
            body.write(" = ?");
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
     *          ]
     *      },
     *      "page": {
     *          "size": 50,
     *          "number": 2
     *      },
     *      "sort": [
     *          { "a": "asc" },
     *          { "b": "desc" }
     *      ]
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
        try {
            Writer body = statement.getBodyWriter();

            body.write("SELECT ");
            body.write(DOCUMENT_COLUMN);
            body.write(" FROM ");
            body.write(collection.toString());

            SearchClauses.writeSearchWhere(statement, criteria);
            SearchClauses.writeSearchOrder(statement, criteria);
            SearchClauses.writeSearchPaging(statement, criteria);
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build search query", e);
        }
    }

}
