package info.smart_tools.smartactors.database_postgresql.postgres_schema;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.database.database_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.database.database_storage.utils.CollectionName;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.search.FieldPath;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.key_tools.Keys;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

/**
 * A set of methods to write some SQL statements during the collection/table altering.
 */
public final class AlterClauses {

    /**
     * Private constructor to avoid instantiation.
     */
    private AlterClauses() {
    }

    /**
     * Writes definition of full text column, i.e. "fulltext_english tsvector".
     * @param body SQL query body to write
     * @param options collection create options to check for fulltext index
     * @throws ResolutionException if failed to resolve field names from IOC
     * @throws InvalidArgumentException if failed to get fulltext field from options
     * @throws IOException if failed to write the body
     */
    public static void writeFullTextColumn(final Writer body, final IObject options)
            throws ResolutionException, InvalidArgumentException, IOException {
        if (options == null) {
            // ignoring absence of fulltext option
            return;
        }
        String fullTextLanguage = PostgresSchema.DEFAULT_FTS_DICTIONARY;
        try {
            IKey fieldKey = Keys.getKeyByName("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");
            IFieldName fullTextField = IOC.resolve(fieldKey, "fulltext");
            Object fullTextDefinition = options.getValue(fullTextField);
            if (fullTextDefinition == null) {
                // ignoring absence of fulltext option
                return;
            }
            IFieldName languageField = IOC.resolve(fieldKey, "language");
            String language = (String) options.getValue(languageField);
            if (language != null) {
                fullTextLanguage = language;
            }
        } catch (ReadValueException e) {
            // ignoring absence of fulltext option
        }
        body.write(PostgresSchema.FULLTEXT_COLUMN + "_" + fullTextLanguage);
        body.write(" tsvector");
    }
}
