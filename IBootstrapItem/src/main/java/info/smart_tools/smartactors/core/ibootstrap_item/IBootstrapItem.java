package info.smart_tools.smartactors.core.ibootstrap_item;

import java.util.function.Function;

/**
 * Interface for atomic step of plugin loading chain
 */
public interface IBootstrapItem {

    IBootstrapItem before(String itemName);
    IBootstrapItem after(String itemName);
    //void process(IBootstrapItemProcess process);
    void process(Function process/*final IBootstrapItemProcess process*/);
}
