package info.smart_tools.smartactors.core.bootstrap_item;

import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Inner class for realize pattern 'Builder' for class {@link BootstrapItem}
 */
class ItemCore {

    /** Item name */
    private String itemName;
    /** Action for loading plugin chain element to the system */
    private IAction process;
    /** Action for unloading plugin chain element to the system */
    private IAction revertProcess;
    /** List of dependencies current plugin from other plugins */
    private List<String> afterList = new ArrayList<>();
    /** List of dependencies other plugins from current */
    private List<String> beforeList = new ArrayList<>();

    /**
     * Constructor for create new instance of {@link ItemCore} by name
     * @param name name of the new {@link ItemCore}
     * @throws InvalidArgumentException if argument null or other errors occurred
     */
    ItemCore(final String name)
            throws InvalidArgumentException {
        if (name == null) {
            throw new InvalidArgumentException("Incoming argument should not be null.");
        }
        this.itemName = name;
    }

    /**
     * Add new 'after' dependency to the dependencies list
     * @param after 'after' dependency
     */
    void addAfter(final String after) {
        this.afterList.add(after);
    }

    /**
     * Add new 'before' dependency to the dependencies list
     * @param before 'before' dependency
     */
    void addBefore(final String before) {
        this.beforeList.add(before);
    }

    /**
     * Add action for loading current item to the server
     * @param action implementation of {@link IAction}
     */
    void setProcess(final IAction action) {
        this.process = action;
    }

    /**
     * Add action for unloading current item from the server
     * @param action implementation of {@link IAction}
     */
    void setRevertProcess(final IAction action) {
        this.revertProcess = action;
    }

    /**
     * Execute process action
     * @param obj acting object
     * @throws ProcessExecutionException if any errors occurred
     */
    void executeProcess(final Object obj)
            throws ProcessExecutionException {
        try {
            this.process.execute(obj);
        } catch (Throwable e) {
            throw new ProcessExecutionException("Process execution failed.", e);
        }
    }

    /**
     * Execute revert process action
     * @param obj acting object
     * @throws RevertProcessExecutionException if any errors occurred
     */
    void executeRevertProcess(final Object obj)
            throws RevertProcessExecutionException {
        try {
            this.revertProcess.execute(obj);
        } catch (Throwable e) {
            throw new RevertProcessExecutionException("Revert process execution failed.", e);
        }
    }

    /**
     * Returns list of 'after' dependencies
     * @return list of 'after' dependencies
     */
    List<String> getAfterList() {
        return this.afterList;
    }

    /**
     * Returns list of 'before' dependencies
     * @return list of 'before' dependencies
     */
    List<String> getBeforeList() {
        return this.beforeList;
    }

    /**
     * Return current item name
     * @return name of item
     */
    String getItemName() {
        return this.itemName;
    }

}
