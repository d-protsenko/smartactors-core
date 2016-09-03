package info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree;

import java.util.Map;

/**
 * Tree for matching strings on template
 */
public interface IParseTree {
    void addTemplate(String template);

    Map<String, String> match(String uri);
}
