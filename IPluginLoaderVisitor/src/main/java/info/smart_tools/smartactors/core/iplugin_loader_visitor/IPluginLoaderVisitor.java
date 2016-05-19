package info.smart_tools.smartactors.core.iplugin_loader_visitor;

import info.smart_tools.smartactors.core.iaction.IBiAction;

/**
 * Interface IPluginLoaderVisitor
 * for implements pattern visitor
 */
public interface IPluginLoaderVisitor {

    /**
     * Handler for plugin loading fail
     * @param action action with two parameters
     */
    void pluginLoadingFail(IBiAction action);

    /**
     * Handler for package loading fail
     * @param action action with two parameters
     */
    void packageLoadingFail(IBiAction action);

    /**
     * Handler for plugin loading success
     * @param action action with two parameters
     */
    void pluginLoadingSuccess(IBiAction action);

    /**
     * Handler for package loading success
     * @param action action with two parameters
     */
    void packageLoadingSuccess(IBiAction action);
}
