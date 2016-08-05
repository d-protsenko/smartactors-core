package info.smart_tools.smartactors.core.postgres_schema;

import info.smart_tools.smartactors.core.bootstrap.Bootstrap;
import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iplugin.exception.PluginException;
import info.smart_tools.smartactors.core.postgres_schema.indexes.IndexCreators;
import info.smart_tools.smartactors.plugin.ifieldname.IFieldNamePlugin;
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
        IObject options = new DSObject("{ \"indexes\": [" +
                    "{ " +
                        "\"fields\": [ \"a\", \"b\" ]," +
                        "\"type\": \"ordered\"" +
                    "}" +
                "]}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((document#>'{a}'),(document#>'{b}'));\n",
                body.toString());
    }

    @Test
    public void testDatetimeIndex() throws Exception {
        IObject options = new DSObject("{ \"indexes\": [" +
                    "{ " +
                        "\"fields\": [ \"a\", \"b\" ]," +
                        "\"type\": \"datetime\"" +
                    "}" +
                "]}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING BTREE ((parse_timestamp_immutable(document#>'{a}')),(parse_timestamp_immutable(document#>'{b}')));\n",
                body.toString());
    }

    @Test
    public void testTagsIndex() throws Exception {
        IObject options = new DSObject("{ \"indexes\": [" +
                    "{ " +
                        "\"fields\": [ \"a\", \"b\" ]," +
                        "\"type\": \"tags\"" +
                    "}" +
                "]}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN ((document#>'{a}'),(document#>'{b}'));\n",
                body.toString());
    }

    @Test
    public void testFulltextIndex() throws Exception {
        IObject options = new DSObject("{ \"indexes\": [" +
                    "{ " +
                        "\"fields\": [ \"a\", \"b\" ]," +
                        "\"type\": \"fulltext\"," +
                        "\"language\": \"russian\"" +
                    "}" +
                "]}");
        IndexCreators.writeIndexes(body, collection, options);
        assertEquals("CREATE INDEX ON test_collection USING GIN ((to_tsvector('russian',(document#>'{a}')::text)),(to_tsvector('russian',(document#>'{b}')::text)));\n",
                body.toString());
    }

    @Test(expected = Exception.class)
    public void testUnsupportedIndexType() throws Exception {
        IObject options = new DSObject("{ \"indexes\": [" +
                    "{ " +
                        "\"fields\": [ \"a\", \"b\" ]," +
                        "\"type\": \"unsupported\"" +
                    "}" +
                "]}");
        IndexCreators.writeIndexes(body, collection, options);
    }

}
