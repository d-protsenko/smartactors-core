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
     * Add
     * @param after
     */
    void addAfter(final String after) {
        this.afterList.add(after);
    }

    /**
     *
     * @param before
     */
    void addBefore(final String before) {
        this.beforeList.add(before);
    }

    /**
     *
     * @param action
     */
    void setProcess(final IAction action) {
        this.process = action;
    }

    /**
     *
     * @param action
     */
    void setRevertProcess(final IAction action) {
        this.revertProcess = action;
    }

    /**
     *
     * @param obj
     * @throws ProcessExecutionException
     */
    void executeProcess(final Object obj)
            throws ProcessExecutionException {
        try {
            this.process.execute(obj);
        } catch (Throwable e) {
            throw new ProcessExecutionException("", e);
        }
    }

    /**
     *
     * @param obj
     * @throws RevertProcessExecutionException
     */
    void executeRevertProcess(final Object obj)
            throws RevertProcessExecutionException {
        try {
            this.revertProcess.execute(obj);
        } catch (Throwable e) {
            throw new RevertProcessExecutionException("", e);
        }
    }
}
