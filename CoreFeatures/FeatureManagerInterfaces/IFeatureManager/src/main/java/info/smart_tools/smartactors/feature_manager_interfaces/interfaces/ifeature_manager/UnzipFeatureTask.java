package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.base.path.Path;
import info.smart_tools.smartactors.configuration_manager.interfaces.iconfiguration_manager.IConfigurationManager;
import info.smart_tools.smartactors.feature_loading_system.bootstrap.Bootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.ProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.exception.RevertProcessExecutionException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap_item.IBootstrapItem;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.IPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.IPluginCreator;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_creator.exception.PluginCreationException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader.IPluginLoader;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin_loader_visitor.IPluginLoaderVisitor;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager.exception.FeatureManagementException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by sevenbits on 11/15/16.
 */
public class UnzipFeatureTask implements ITask {

    private IFeatureManager featureManager;
    private IFeature<String, IFeatureState<String>> feature;

    public UnzipFeatureTask(final IFeatureManager manager, final IFeature feature)
            throws ResolutionException {

        this.featureManager = manager;
        this.feature = feature;
    }

    public void execute()
            throws TaskExecutionException {
        try {
            System.out.println("Start unzipping feature - '" + feature.getName() + "'.");



            ((IFeatureState) this.feature.getStatus()).next();
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(true);
            ((IFeatureState) this.feature.getStatus()).setExecuting(false);
            System.out.println("Feature - '" + feature.getName() + "' has been unzipped successful.");
        } catch (Throwable e) {
            ((IFeatureState) this.feature.getStatus()).setLastSuccess(false);
            System.out.println("Unzipping - '" + feature.getName() + "was breaking with exception:");
            System.err.println(e);
        }
        try {
            this.featureManager.onCompleteFeatureOperation(this.feature);
        } catch (FeatureManagementException e) {
            System.err.println(e);
        }
    }
}
