package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.indexes.IndexCreators;
import info.smart_tools.smartactors.core.postgres_schema.search.FieldPath;
import info.smart_tools.smartactors.core.postgres_schema.search.PostgresFieldPath;

import java.io.IOException;
import java.io.Writer;

/**
 * A set of static methods to take statements to be executed in the Postgres database.
 * The statements are to perform operations over jsonb documents.
 */
public final class PostgresSchema {

    /**
     * Name of the DOCUMENT column.
     */
    public static final String DOCUMENT_COLUMN = "document";

    /**
     * Name of the column for fulltext search.
     */
    public static final String FULLTEXT_COLUMN = "fulltext";

    /**
     * Pattern for the document field with the document ID.
     */
    public static final String ID_FIELD_PATTERN = "%sID";

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
            body.write(DOCUMENT_COLUMN);
            body.write(" jsonb NOT NULL");
            writeFullTextColumn(body, options);
            body.write(");\n");
            writePrimaryKey(body, collection);
            if (options != null) {
                IndexCreators.writeIndexes(body, collection, options);
            }
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build create body", e);
        }
    }

    private static FieldPath getIdFieldPath(CollectionName collection) throws QueryBuildException {
        return PostgresFieldPath.fromString(String.format(ID_FIELD_PATTERN, collection.toString()));
    }

    private static void writePrimaryKey(final Writer body, final CollectionName collection) throws IOException, QueryBuildException {
        String collectionName = collection.toString();
        FieldPath idPath = getIdFieldPath(collection);
        body.write("CREATE UNIQUE INDEX ");
        body.write(collectionName);
        body.write("_pkey ON ");
        body.write(collectionName);
        body.write(" USING BTREE ((");
        body.write(idPath.toSQL());
        body.write("));\n");
    }

    private static void writeFullTextColumn(final Writer body, final IObject options)
            throws ResolutionException, InvalidArgumentException, IOException {
        if (options == null) {
            // ignoring absence of fulltext option
            return;
        }
        try {
            IKey fieldKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
            IFieldName fullTextField = IOC.resolve(fieldKey, "fulltext");
            Object fullTextDefinition = options.getValue(fullTextField);
            if (fullTextDefinition == null) {
                // ignoring absence of fulltext option
                return;
            }
        } catch (ReadValueException e) {
            // ignoring absence of fulltext option
        }
        body.write(", ");
        body.write(FULLTEXT_COLUMN);
        body.write(" tsvector");
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
            body.write(DOCUMENT_COLUMN);
            body.write(") VALUES (?::jsonb)");
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
            body.write(" SET ");
            body.write(DOCUMENT_COLUMN);
            body.write(" = ?::jsonb WHERE (");
            body.write(getIdFieldPath(collection).toSQL());
            body.write(") = to_json(?)::jsonb");
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
            body.write(" WHERE (");
            body.write(getIdFieldPath(collection).toSQL());
            body.write(") = to_json(?)::jsonb");
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

            if (criteria == null) {
                return;
            }
            SearchClauses.writeSearchWhere(statement, criteria);
            SearchClauses.writeSearchOrder(statement, criteria);
            SearchClauses.writeSearchPaging(statement, criteria);
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build search query", e);
        }
    }

}
