package info.smart_tools.smartactors.core.ibootstrap_item;


import info.smart_tools.smartactors.core.iaction.IAction;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.ProcessExecutionException;
import info.smart_tools.smartactors.core.ibootstrap_item.exception.RevertProcessExecutionException;

/**
 * Interface for atomic step of plugin loading chain
 */
public interface IBootstrapItem {

    /**
     * Add name of {@link IBootstrapItem} instance what should be loaded before current item
     * Must support fluent interface
     * @param itemName name of {@link IBootstrapItem} instance what should be loaded before current item
     * @return current instance of {@link IBootstrapItem}
     */
    IBootstrapItem before(String itemName);

    /**
     * Add name of {@link IBootstrapItem} instance what should be loaded after current item
     * Must support fluent interface
     * @param itemName name of {@link IBootstrapItem} instance what should be loaded after current item
     * @return current instance of {@link IBootstrapItem}
     */
    IBootstrapItem after(String itemName);

    /**
     * Action for execution for loading current item to the system
     * Must support fluent interface
     * @param process instance of {@link IAction} or function
     * @return current instance of {@link IBootstrapItem}
     */
    IBootstrapItem process(IAction process);

    /**
     * Action for revert process action (unload current item from the server)
     * Must support fluent interface
     * @param process instance of {@link IAction} or function
     * @return current instance of {@link IBootstrapItem}
     */
    IBootstrapItem revertProcess(IAction process);

    /**
     * Execute process action
     * @param object action object
     * @throws ProcessExecutionException if any error occurred
     */
    void executeProcess(Object object) throws ProcessExecutionException;

    /**
     * Execute revert process action
     * @param object acting object
     * @throws RevertProcessExecutionException if any error occurred
     */
    void executeRevertProcess(Object object) throws RevertProcessExecutionException;
}
