package info.smart_tools.smartactors.core.deserialize_strategy_get.parse_tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sevenbits on 03.09.16.
 */
public class TreeObject {
    private int level;
    private List<String> uri;
    private Map<String, String> result;
    private boolean ended;

    public TreeObject(int level, List<String> uri) {
        this.level = level;
        this.uri = uri;
        this.result = new HashMap<>();
        ended = false;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUriElem(int index) {
        return uri.get(index);
    }

    public void setUri(List<String> uri) {
        this.uri = uri;
    }

    public void addResult(String key, String value) {
        this.result.put(key, value);
    }

    public Map<String, String> getResult() {
        return result;
    }

    public boolean hasElem(int index) {
        return uri.size() > index;
    }

    public List<String> getUri() {
        return uri;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }
}
