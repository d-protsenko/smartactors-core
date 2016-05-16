package info.smart_tools.smartactors.core.bootstrap_item;

import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;

import java.util.function.Function;

/**
 * Implementation of {@link IBootstrapItem}
 */
public class BootstrapItem implements IBootstrapItem {

    private ItemCore item;

    public BootstrapItem(final String name) {
        item = new ItemCore(name);
    }

    @Override
    public BootstrapItem before(final String itemName) {
        item.addBefore(itemName);
        return this;
    }

    @Override
    public BootstrapItem after(final String itemName) {
        item.addAfter(itemName);
        return this;
    }

    @Override
    public void process(final Function process/*final IBootstrapItemProcess process*/) {
        item.setProcess(process);
    }
}
