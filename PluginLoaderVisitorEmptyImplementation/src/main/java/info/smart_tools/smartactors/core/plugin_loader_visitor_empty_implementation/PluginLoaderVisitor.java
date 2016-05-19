package info.smart_tools.smartactors.core.plugin_loader_visitor_empty_implementation;

import info.smart_tools.smartactors.core.iaction.IBiAction;
import info.smart_tools.smartactors.core.iplugin_loader_visitor.IPluginLoaderVisitor;

/**
 * Empty implementation of {@link IPluginLoaderVisitor}
 */
public class PluginLoaderVisitor implements IPluginLoaderVisitor {

    @Override
    public void pluginLoadingFail(final IBiAction action) {

    }

    @Override
    public void packageLoadingFail(final IBiAction action) {

    }

    @Override
    public void pluginLoadingSuccess(final IBiAction action) {

    }

    @Override
    public void packageLoadingSuccess(final IBiAction action) {

    }
}
