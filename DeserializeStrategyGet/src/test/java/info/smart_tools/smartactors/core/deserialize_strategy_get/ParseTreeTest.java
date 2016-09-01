package info.smart_tools.smartactors.core.deserialize_strategy_get;

import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.ParseTree;
import info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree.Template;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by sevenbits on 01.09.16.
 */
public class ParseTreeTest {
    @Before
    public void setUp() {

    }

    @Test
    public void testTemplateAddition() {
        ParseTree tree = new ParseTree(0);
        tree.addTemplate(new Template("/home/:messageMapId/abrakadabra"));
        tree.match(Arrays.asList("/home/123/abrakadabra".split("/")));
    }
}
