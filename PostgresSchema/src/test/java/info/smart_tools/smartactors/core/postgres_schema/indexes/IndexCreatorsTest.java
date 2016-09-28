package info.smart_tools.smartactors.core.postgres_schema.indexes;

import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.iobject_plugins.ifieldname_plugin.IFieldNamePlugin;
import info.smart_tools.smartactors.plugin.ioc_keys.PluginIOCKeys;
import info.smart_tools.smartactors.plugin.ioc_simple_container.PluginIOCSimpleContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Test for different index creation statements.
 */
public class IndexCreatorsTest {

    private CollectionName collection;
    private StringWriter body;

    @BeforeClass
    public static void initIOC() throws PluginException, ProcessExecutionException {
        Bootstrap bootstrap = new Bootstrap();
        new PluginIOCSimpleContainer(bootstrap).load();
        new PluginIOCKeys(bootstrap).load();
        new IFieldNamePlugin(bootstrap).load();
        bootstrap.start();
    }

    @Before
    public void setUp() throws QueryBuildException {
        collection = CollectionName.fromString("test_collection");
        body = new StringWriter();
    }

    @Test
    public void testOrderedIndex() throws Exception {
        IObject options = new DSObject("{ \"ordered\": [ \"a\", \"b\" ] }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((document#>'{a}'));\n" +
                     "CREATE INDEX ON test_collection USING BTREE ((document#>'{b}'));\n",
                body.toString());
    }

    @Test
    public void testOrderedIndexForOneField() throws Exception {
        IObject options = new DSObject("{ \"ordered\": \"a\" }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((document#>'{a}'));\n",
                body.toString());
    }

    @Test
    public void testDatetimeIndex() throws Exception {
        IObject options = new DSObject("{ \"datetime\": [ \"a\", \"b\" ] }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((parse_timestamp_immutable(document#>'{a}')));\n" +
                     "CREATE INDEX ON test_collection USING BTREE ((parse_timestamp_immutable(document#>'{b}')));\n",
                body.toString());
    }

    @Test
    public void testDatetimeIndexForOneField() throws Exception {
        IObject options = new DSObject("{ \"datetime\": \"a\" }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((parse_timestamp_immutable(document#>'{a}')));\n",
                body.toString());
    }

    @Test
    public void testTagsIndex() throws Exception {
        IObject options = new DSObject("{ \"tags\": [ \"a\", \"b\" ] }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN ((document#>'{a}'));\n" +
                     "CREATE INDEX ON test_collection USING GIN ((document#>'{b}'));\n",
                body.toString());
    }

    @Test
    public void testTagsIndexForOneField() throws Exception {
        IObject options = new DSObject("{ \"tags\": \"a\" }");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN ((document#>'{a}'));\n",
                body.toString());
    }

    @Test
    public void testFulltextIndex() throws Exception {
        IObject options = new DSObject("{ " +
                "\"fulltext\": [ \"a\", \"b\" ]," +
                "\"language\": \"russian\"" +
                "}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN (fulltext);\n" +
                        "CREATE FUNCTION test_collection_fulltext_update_trigger() RETURNS trigger AS $$\n" +
                        "begin\n" +
                        "new.fulltext := " +
                        "to_tsvector('russian', coalesce((new.document#>'{a}')::text,'') || ' ' || coalesce((new.document#>'{b}')::text,''));\n" +
                        "return new;\n" +
                        "end\n" +
                        "$$ LANGUAGE plpgsql;\n" +
                        "CREATE TRIGGER test_collection_fulltext_update_trigger BEFORE INSERT OR UPDATE " +
                        "ON test_collection FOR EACH ROW EXECUTE PROCEDURE " +
                        "test_collection_fulltext_update_trigger();\n",
                body.toString());
    }

    @Test
    public void testFulltextIndexForOneField() throws Exception {
        IObject options = new DSObject("{ " +
                "\"fulltext\": \"text\"," +
                "\"language\": \"russian\"" +
                "}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN (fulltext);\n" +
                        "CREATE FUNCTION test_collection_fulltext_update_trigger() RETURNS trigger AS $$\n" +
                        "begin\n" +
                        "new.fulltext := " +
                        "to_tsvector('russian', coalesce((new.document#>'{text}')::text,''));\n" +
                        "return new;\n" +
                        "end\n" +
                        "$$ LANGUAGE plpgsql;\n" +
                        "CREATE TRIGGER test_collection_fulltext_update_trigger BEFORE INSERT OR UPDATE " +
                        "ON test_collection FOR EACH ROW EXECUTE PROCEDURE " +
                        "test_collection_fulltext_update_trigger();\n",
                body.toString());
    }

    @Test(expected = Exception.class)
    public void testFullTextIndexWithoutLanguage() throws Exception {
        IObject options = new DSObject("{ \"fulltext\": \"text\" }");
        IndexCreators.writeIndexes(body, collection, options);
    }

    @Test(expected = Exception.class)
    public void testWrongIndexFormat() throws Exception {
        IObject options = new DSObject("{ \"ordered\": 123 }");
        IndexCreators.writeIndexes(body, collection, options);
    }

}
