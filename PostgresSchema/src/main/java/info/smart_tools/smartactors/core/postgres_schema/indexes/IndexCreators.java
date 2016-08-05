package info.smart_tools.smartactors.core.postgres_schema.indexes;

import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_schema.search.FieldPath;
import info.smart_tools.smartactors.core.postgres_schema.search.PostgresFieldPath;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A set of classes to write CREATE INDEX SQL statements.
 */
public class IndexCreators {

    /**
     * Set of index creation functions.
     */
    private static final Map<String, IndexWriterResolver> INDEX_WRITERS = new HashMap<String, IndexWriterResolver>() {{
        put("ordered", (indexDefinition) -> IndexCreators::writeOrderedIndex);
        put("datetime", (indexDefinition) -> IndexCreators::writeDatetimeIndex);
        put("tags", (indexDefinition) -> IndexCreators::writeTagsIndex);
        put("fulltext", FulltextIndexWriter::resolve);
    }};

    /**
     * Private constructor to avoid instantiation.
     */
    private IndexCreators() {
    }

    /**
     * Writes CREATE INDEX statements to the SQL statement body.
     * @param body where to write SQL
     * @param collection name of the collection
     * @param options document describing create collection options
     * @throws Exception when something goes wrong
     */
    public static void writeIndexes(Writer body, CollectionName collection, IObject options) throws Exception {
        try {
            IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
            IFieldName indexesField = IOC.resolve(fieldNameKey, "indexes");
            List<IObject> indexes = (List<IObject>) options.getValue(indexesField);
            if (indexes == null) {
                // no indexes definition, ignoring
                return;
            }
            for (IObject indexDefinition : indexes) {
                writeCreateIndex(body, collection, indexDefinition);
            }
        } catch (ReadValueException e) {
            // no indexes definition, ignoring
        }
    }

    private static void writeCreateIndex(final Writer body, final CollectionName collection, IObject indexDefinition)
            throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        IFieldName typeField = IOC.resolve(fieldNameKey, "type");
        String indexType = ((String) indexDefinition.getValue(typeField)).toLowerCase();
        IFieldName fieldsField = IOC.resolve(fieldNameKey, "fields");
        List<String> fieldNames = (List<String>) indexDefinition.getValue(fieldsField);

        List<FieldPath> fieldPaths = new ArrayList<>();
        for (String fieldName : fieldNames) {
            fieldPaths.add(PostgresFieldPath.fromString(fieldName));
        }

        body.write("CREATE INDEX ON ");
        body.write(collection.toString());
        body.write(" USING ");
        INDEX_WRITERS.get(indexType).resolve(indexDefinition).write(body, fieldPaths);
        body.write(";\n");
    }

    private static void writeOrderedIndex(final Writer body, final List<FieldPath> fields) throws IOException {
        body.write("BTREE (");
        Iterator<FieldPath> i = fields.iterator();
        while (i.hasNext()) {
            FieldPath field = i.next();
            body.write("(");
            body.write(field.toSQL());
            body.write(")");
            if (i.hasNext()) {
                body.write(",");
            }
        }
        body.write(")");
    }

    private static void writeDatetimeIndex(final Writer body, final List<FieldPath> fields) throws IOException {
        body.write("BTREE (");
        Iterator<FieldPath> i = fields.iterator();
        while (i.hasNext()) {
            FieldPath field = i.next();
            body.write("(parse_timestamp_immutable(");
            body.write(field.toSQL());
            body.write("))");
            if (i.hasNext()) {
                body.write(",");
            }
        }
        body.write(")");
    }

    private static void writeTagsIndex(final Writer body, final List<FieldPath> fields) throws IOException {
        body.write("GIN (");
        Iterator<FieldPath> i = fields.iterator();
        while (i.hasNext()) {
            FieldPath field = i.next();
            body.write("(");
            body.write(field.toSQL());
            body.write(")");
            if (i.hasNext()) {
                body.write(",");
            }
        }
        body.write(")");
    }

}
