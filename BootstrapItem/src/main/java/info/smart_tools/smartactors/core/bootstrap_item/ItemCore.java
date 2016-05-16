package info.smart_tools.smartactors.core.bootstrap_item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by andrey on 5/17/16.
 */
class ItemCore {
    private String itemName;
    //private IBootstrapItemProcess process;
    private Function process;
    private List<String> afterList = new ArrayList<>();
    private List<String> beforeList = new ArrayList<>();

    ItemCore(final String name) {
        this.itemName = name;
    }

    void addAfter(final String after) {
        this.afterList.add(after);
    }

    void addBefore(final String before) {
        this.beforeList.add(before);
    }

    void setProcess(final Function process) {
        this.process = process;
    }
}
