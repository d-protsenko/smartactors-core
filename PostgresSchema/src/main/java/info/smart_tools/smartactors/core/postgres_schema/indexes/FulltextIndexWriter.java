package info.smart_tools.smartactors.core.postgres_schema.indexes;

import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.postgres_schema.search.FieldPath;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * Writes fulltext index.
 */
class FulltextIndexWriter implements IndexWriter {

    static IndexWriter resolve(IObject indexDefinition) throws Exception {
        IKey fieldNameKey = Keys.getOrAdd(IFieldName.class.getCanonicalName());
        IFieldName languageField = IOC.resolve(fieldNameKey, "language");
        String language = (String) indexDefinition.getValue(languageField);
        return new FulltextIndexWriter(language);
    }

    private final String language;

    private FulltextIndexWriter(String language) {
        this.language = language;
    }

    @Override
    public void write(Writer body, List<FieldPath> fields) throws IOException {
        body.write("GIN (");
        Iterator<FieldPath> i = fields.iterator();
        while (i.hasNext()) {
            FieldPath field = i.next();
            body.write("(to_tsvector('");
            body.write(language);   // TODO: avoid SQL injection
            body.write("',(");
            body.write(field.toSQL());
            body.write(")::text))");
            if (i.hasNext()) {
                body.write(",");
            }
        }
        body.write(")");
    }

}
