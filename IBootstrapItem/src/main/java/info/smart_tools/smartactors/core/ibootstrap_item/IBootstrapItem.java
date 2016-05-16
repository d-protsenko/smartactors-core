package info.smart_tools.smartactors.core.ibootstrap_item;

/**
 * Interface for atomic step of plugin loading chain
 */
public interface IBootstrapItem {

    void before(String itemName);
    void after(String itemName);
    void process();
}
