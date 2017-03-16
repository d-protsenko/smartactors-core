package info.smart_tools.smartactors.das.commands;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.das.utilities.CommandLineArgsResolver;
import info.smart_tools.smartactors.das.utilities.ProjectResolver;
import info.smart_tools.smartactors.das.models.Feature;
import info.smart_tools.smartactors.das.models.Plugin;
import info.smart_tools.smartactors.das.models.Project;

import java.util.ArrayList;
import java.util.List;

public class UpdatePluginVersion implements IAction {

    @Override
    public void execute(Object o)
            throws ActionExecuteException, InvalidArgumentException {
        System.out.println("Updating plugin version ...");

        try {
            String[] args = (String[]) o;
            ProjectResolver pr = new ProjectResolver();
            Project project = pr.resolveProject();

            CommandLineArgsResolver clar = new CommandLineArgsResolver(args);
            String newVersion = clar.getVersion();
            Feature feature = null;
            String featureName = clar.getFeatureName();
            List<Feature> features = new ArrayList<>();
            if (null != featureName) {
                feature = project.getFeatures().stream().filter(f -> f.getName().equals(featureName)).findFirst().orElse(null);
            }
            if (null == feature) {
                feature = pr.getCurrentFeature();
            }
            if (null != feature) {
                features.add(feature);
            }
            if (null == featureName || featureName.toLowerCase().equals("all")) {
                features = project.getFeatures();
            }
            String pluginName = clar.getPluginName();

            for(Feature f : features) {
                System.out.println("Feature - " + f.getName() + " ...");
                List<Plugin> plugins = new ArrayList<>();
                if (null == pluginName || pluginName.toLowerCase().equals("all")) {
                    plugins.addAll(f.getPlugins());
                } else {
                    Plugin p = f.getPlugins().stream()
                            .filter(pl -> pl.getRawName().equals(pluginName)).findFirst().orElse(null);
                    if (null != p) {
                        plugins.add(p);
                    }
                }
                for (Plugin p : plugins) {
                    System.out.println("Plugin - " + p.getRawName() + " ...");
                    p.setVersion(newVersion);
                    p.updateVersionInPom(newVersion);
                    System.out.println("... processed.");
                }
            }

            // Save project meta data file
            project.saveMetaDataFile();

        } catch (Exception e) {
            System.out.println("Update plugin version has been failed.");
            System.err.println(e);

            throw new ActionExecuteException(e);
        }
        System.out.println("Plugin version has been updated successful.");
    }
}
