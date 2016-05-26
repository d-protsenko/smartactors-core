package info.smart_tools.smartactors.core.bootstrap;

import info.smart_tools.smartactors.core.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.core.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Implementation of {@link IBootstrap}
 */
public class Bootstrap implements IBootstrap<IBootstrapItem<String>> {

    //private List<IBootstrapItem> itemStorage = new ArrayList<>();
    private Map<String, IBootstrapItem<String>> itemStorage = new HashMap<>();

    @Override
    public void add(final IBootstrapItem<String> bootstrapItem) {
        itemStorage.put(bootstrapItem.getItemName(), bootstrapItem);
    }

    @Override
    public void start()
            throws ProcessExecutionException {
        try {
            TopologicalSort ts = new TopologicalSort(itemStorage);
            String[] orderedItems = ts.getOrderedList();

        } catch (Throwable e) {
            throw  new ProcessExecutionException("Could not execute plugin process.", e);
        }
    }

    @Override
    public void revert()
            throws RevertProcessExecutionException {
//        try {
//
//        } catch (Throwable e) {
//            throw new RevertProcessExecutionException("Could not execute plugin revert process.", e);
//        }
    }

    /**
     * Sort given list of {@link IBootstrapItem} using topological graph sorting
     * @return sorted list of {@link IBootstrapItem} names
     */


    private boolean dfs(int v) {
        if ()
    }

}

class TopologicalSort {
    private boolean cycle;
    private int[] colors;
    private Stack stack = new Stack();
    private String[] orderedList;
    private int size;
    private List<String>[] edges;
    private Map<String, IBootstrapItem<String>> items;

    public TopologicalSort(final Map<String, IBootstrapItem<String>> items) {
        this.items = items;
        this.size = items.size();
        this.edges = new ArrayList[this.size];
        this.orderedList = new String[this.size];
        this.colors = new int[this.size];
        sortItems();
    }

    private void sortItems() {

        // fill lists of edges
        int count = 0;
        for (Map.Entry<String, IBootstrapItem<String>> pair : items.entrySet()) {
            String name = pair.getValue().getItemName();

            // fill 'after' dependencies
            edges[count] = pair.getValue().getAfterItems();

            // loop for find dependencies from other items
            for (Map.Entry<String, IBootstrapItem<String>> innerPair : items.entrySet()) {
                for (String before : innerPair.getValue().getBeforeItems()) {
                    if (before.equals(name)) {
                        edges[count].add(before);
                    }
                }
            }
            ++count;
        }
    }

    private boolean dfs(int v) {
        if (1 == colors[v]) {
            return true;
        }
        if (2 == colors[v]) {
            return false;
        }
        colors[v] = 1;
        for (int i = 0; i < this.edges[v].size(); ++i) {
            if (dfs(this.edges[v].get(i))) {

            }
        }

    }

    public String[] getOrderedList() {
        return this.orderedList;
    }
}
