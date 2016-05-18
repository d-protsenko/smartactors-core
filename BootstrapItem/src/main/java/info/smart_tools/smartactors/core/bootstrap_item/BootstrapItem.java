package info.smart_tools.smartactors.core.bootstrap_item;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;

import java.util.List;

/**
 * Implementation of {@link IBootstrapItem}
 */
public class BootstrapItem implements IBootstrapItem {

    private ItemCore item;

    /**
     * Constructor with item name as argument
     * @param name name of {@link IBootstrapItem} instance
     * @throws InvalidArgumentException if any errors occurred
     */
    public BootstrapItem(final String name) throws InvalidArgumentException {
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
    public BootstrapItem process(final IAction action) {
        item.setProcess(action);
        return this;
    }

    @Override
    public BootstrapItem revertProcess(final IAction action) {
        item.setRevertProcess(action);
        return this;
    }

    @Override
    public void executeProcess(final Object object)
            throws ProcessExecutionException {
        item.executeProcess(object);
    }

    @Override
    public void executeRevertProcess(final Object object)
            throws RevertProcessExecutionException {
        item.executeRevertProcess(object);
    }

    @Override
    public List<String> getBeforeItems() {
        return this.item.getBeforeList();
    }

    @Override
    public List<String> getAfterItems() {
        return this.item.getAfterList();
    }

    @Override
    public String getItemName() {
        return item.getItemName();
    }
}
