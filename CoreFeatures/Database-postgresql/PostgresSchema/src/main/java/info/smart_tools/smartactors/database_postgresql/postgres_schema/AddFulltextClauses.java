package info.smart_tools.smartactors.database_postgresql.postgres_schema;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.database.database_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.indexes.IndexCreators;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.search.FieldPath;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.search.PostgresFieldPath;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A set of methods to write some SQL statements during the collection/table altering.
 */
final class AddFulltextClauses {

    private final String language;
    private final String columnName;
    private List<FieldPath> fields;

    /**
     * The constructor to get and store params.
     * @param options 'add fulltext' options to store
     * @throws QueryBuildException if failed to write the body
     * @throws ResolutionException if failed to resolve field names from IOC
     * @throws InvalidArgumentException if failed to get fulltext field from options
     */
    AddFulltextClauses(final IObject options)
            throws QueryBuildException, ResolutionException, InvalidArgumentException {
        if (options == null) {
            throw new QueryBuildException("Failed to build 'add fulltext' body");
        }
        try {
            IKey fieldKey = Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");

            IFieldName languageField = IOC.resolve(fieldKey, "language");
            String fullTextLanguage = (String) options.getValue(languageField);
            if (fullTextLanguage == null) {
                language = PostgresSchema.DEFAULT_FTS_DICTIONARY;
            } else {
                language = fullTextLanguage;
            }
            columnName = PostgresSchema.FULLTEXT_COLUMN + "_" + language;

            IFieldName fulltextDefinitionField = IOC.resolve(fieldKey, "fulltext");
            Object fulltextFields = options.getValue(fulltextDefinitionField);
            if (fulltextFields == null) {
                throw new QueryBuildException("Failed to build 'add fulltext' body");
            }

            fields = new ArrayList<>();
            if (fulltextFields instanceof String) {
                fields.add(PostgresFieldPath.fromString((String) fulltextFields));
            } else if (fulltextFields instanceof List) {
                for (Object fieldName : (List) fulltextFields) {
                    fields.add(PostgresFieldPath.fromString((String) fieldName));
                }
            } else {
                throw new QueryBuildException("Unknown definition for 'fulltext' option: " + fulltextFields);
            }
        } catch (ReadValueException e) {
            throw new QueryBuildException("Failed to build 'add fulltext' body", e);
        }
    }

    /**
     * Writes definition of full text column, i.e. "fulltext_english tsvector".
     * @param body SQL query body to write
     * @throws IOException if failed to write the body
     */
    void writeFulltextColumn(final Writer body)
            throws IOException {
        body.write(columnName);
        body.write(" tsvector");
    }

    /**
     * Writes expression for fulltext column update,
     * i.e. "UPDATE collectionName SET fulltext_english := to_tsvector('english', ...)".
     * @param body SQL query body to write
     * @throws IOException if failed to write the body
     */
    void writeFulltextColumnUpdate(final Writer body, final CollectionName collection)
            throws IOException {
        body.write("UPDATE ");
        body.write(collection.toString());
        body.write(" SET ");
        body.write(columnName);
        body.write(" := to_tsvector('");
        body.write(language);   // TODO: avoid SQL injection
        body.write("', ");
        Iterator<FieldPath> i = fields.iterator();
        while (i.hasNext()) {
            FieldPath field = i.next();
            body.write("coalesce((");
            body.write(field.toSQL());
            body.write(")::text,'')");
            if (i.hasNext()) {
                body.write(" || ' ' || ");
            }
        }
        body.write(");\n");
    }

    /**
     * Writes expression of fulltext index creation.
     * @param body SQL query body to write
     * @throws Exception if failed to write the body
     */
    void writeFulltextIndex(final Writer body, final CollectionName collection, final IObject options)
            throws Exception {
        IndexCreators.writeFulltextIndex(body, collection, options, fields);
    }
}
