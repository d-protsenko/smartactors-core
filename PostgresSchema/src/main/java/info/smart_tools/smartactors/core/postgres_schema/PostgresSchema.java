package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.core.postgres_schema.search.*;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Set of index creation templates.
     * Each template can be passed to String.format()
     * and has parameters:
     * 1 - collection name
     * 2 - document field access expression
     * 3 - index parameter
     */
    private static final Map<String, String> INDEX_CREATION_TEMPLATES = new HashMap<String, String>() {{
        put("ordered", "CREATE INDEX ON %1$s USING BTREE ((%2$s));\n");
//        put("tags", "CREATE INDEX ON %1$s USING GIN ((%2$s));\n");
        put("fulltext", "CREATE INDEX ON %1$s USING GIN ((to_tsvector('%3$s',(%2$s)::text)));\n");
//        put("datetime", "CREATE INDEX ON %1$s USING BTREE ((parse_timestamp_immutable(%2$s)));\n");
    }};

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
     * @param indexes document describing a set of indexes to create
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void create(final QueryStatement statement, final CollectionName collection, IObject indexes) throws QueryBuildException {
        Writer body = statement.getBodyWriter();
        try {
            body.write("CREATE TABLE ");
            body.write(collection.toString ());
            body.write(" (");
            body.write(ID_COLUMN);
            body.write(" bigserial PRIMARY KEY, ");
            body.write(DOCUMENT_COLUMN);
            body.write(" jsonb NOT NULL);\n");
            if (indexes != null) {
                for (Map.Entry<IFieldName, Object> entry : indexes) {
                    writeCreateIndex(body, collection, entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build create body", e);
        }
    }

    private static void writeCreateIndex(final Writer body, final CollectionName collection,
                                         final IFieldName fieldName, final Object indexDefinition)
            throws QueryBuildException, IOException {
        String indexType = null;
        String indexParameter = null;
        if (indexDefinition instanceof String) {
            indexType = ((String) indexDefinition).toLowerCase();
        } else if (indexDefinition instanceof IObject) {
            Map.Entry<IFieldName, Object> entry = ((IObject) indexDefinition).iterator().next();
            indexType = entry.getKey().toString().toLowerCase();
            indexParameter = entry.getValue().toString();
        }
        String template = INDEX_CREATION_TEMPLATES.get(indexType);
        FieldPath field = PostgresFieldPath.fromString(fieldName.toString());
        body.write(String.format(template, collection.toString(), field.toSQL(), indexParameter));
    }

    /**
     * Fills the statement body with the sequence name for the collection for 'nextval' query
     * to select the next document ID from the database.
     * @param statement statement to fill the body
     * @param collection collection name to use to construct the sequence name
     * @throws QueryBuildException if the statement body cannot be built
     */
    public static void nextId(final QueryStatement statement, final CollectionName collection) throws QueryBuildException {
        Writer body = statement.getBodyWriter();
        try {
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
        Writer body = statement.getBodyWriter();
        try {
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
        Writer body = statement.getBodyWriter();
        try {
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
        Writer body = statement.getBodyWriter();
        try {
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

            writeSearchWhere(statement, criteria);
            writeSearchOrder(statement, criteria);
            writeSearchPaging(statement, criteria);
        } catch (Exception e) {
            throw new QueryBuildException("Failed to build search query", e);
        }
    }

    private static void writeSearchWhere(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName filterField = IOC.resolve(fieldNameKey, "filter");
            IObject filter = (IObject) criteria.getValue(filterField);
            body.write(" WHERE ");
            PostgresQueryWriterResolver resolver = new PostgresQueryWriterResolver();
            resolver.resolve(null).write(statement, resolver, null, filter);
        } catch (ReadValueException e) {
            // no filter in the criteria, ignoring
        }
    }

    private static void writeSearchOrder(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName sortField = IOC.resolve(fieldNameKey, "sort");
            List<IObject> sortItems = (List<IObject>) criteria.getValue(sortField);
            if (sortItems == null || sortItems.isEmpty()) {
                return; // no sort in the criteria, ignoring
            }
            body.write(" ");
            OrderWriter order = new OrderWriter();
            order.write(statement, sortItems);
        } catch (ReadValueException e) {
            // no sort in the criteria, ignoring
        }
    }

    private static void writeSearchPaging(QueryStatement statement, IObject criteria) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        Writer body = statement.getBodyWriter();
        try {
            IFieldName pageField = IOC.resolve(fieldNameKey, "page");
            IObject page = (IObject) criteria.getValue(pageField);
            if (page == null) {
                return; // no page in the criteria, ignoring
            }
            body.write(" ");
            Integer size;
            Integer number;
            try {
                IFieldName sizeField = IOC.resolve(fieldNameKey, "size");
                size = (Integer) page.getValue(sizeField);
                IFieldName numberField = IOC.resolve(fieldNameKey, "number");
                number = (Integer) page.getValue(numberField);
            } catch (Exception e) {
                throw new QueryBuildException("wrong page format: " + page.serialize(), e);
            }
            PagingWriter paging = new PagingWriter();
            paging.write(statement, number, size);
        } catch (ReadValueException e) {
            // no page in the criteria, ignoring
        }
    }

}
