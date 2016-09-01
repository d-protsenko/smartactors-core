package info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sevenbits on 01.09.16.
 */
public class ParseTree {
    int level;
    ParseTree variableChildElem;
    List<ParseTree> constChildElems;
    List<Template> templates;
    String templateElem;
    boolean isVariableVertex = false;

    public ParseTree(final int level) {
        constChildElems = new ArrayList<>();
        this.level = level;
    }

    public Map<String, String> match(List<String> uri) {
        if (uri.size() <= level) {
            return null;
        }
        if (!templateElem.startsWith(":") && !templateElem.equals(uri.get(level))) {
            return null;
        }
        Map<String, String> resultMap = new HashMap<>();
        //check templates with full equality of current template elem
        for (int i = 0; i < constChildElems.size(); i++) {
            Map<String, String> bufMap;
            bufMap = constChildElems.get(i).match(uri);
            if (bufMap != null && bufMap.size() > resultMap.size()) {
                resultMap = bufMap;
            }
        }

        if (variableChildElem != null) {
            Map<String, String> bufMap;
            bufMap = variableChildElem.match(uri);
            if (bufMap != null && bufMap.size() > resultMap.size()) {
                resultMap = bufMap;
            }
        }
        if (isVariableVertex) {
            resultMap.put(templateElem.substring(1), uri.get(level));
        }
        return resultMap;
    }

    String getTemplateElem() {
        return templateElem;
    }

    public void addTemplate(Template template) {
        if (template.size() <= level) {
            return;
        }
        this.templateElem = template.get(level);
        if (template.isVariable(level)) {
            variableChildElem = new ParseTree(level + 1);
            variableChildElem.addTemplate(template);
            return;
        }
        for (ParseTree constTreeElem : constChildElems) {
            if (constTreeElem.getTemplateElem().equals(template.get(level))) {
                constTreeElem.addTemplate(template);
                return;
            }
        }
        ParseTree treeElem = new ParseTree(level + 1);
        constChildElems.add(treeElem);
        treeElem.addTemplate(template);
    }
}