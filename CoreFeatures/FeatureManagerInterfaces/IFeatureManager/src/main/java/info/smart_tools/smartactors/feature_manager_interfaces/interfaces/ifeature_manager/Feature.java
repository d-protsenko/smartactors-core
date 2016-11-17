package info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature_manager;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_manager_interfaces.interfaces.ifeature.IFeature;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

import java.util.Set;

/**
 * Created by sevenbits on 11/14/16.
 */
public class Feature implements IFeature<String, IFeatureState<String>> {

    private String name;
    private Set<String> dependencies;
    private IPath featureLocation;
    private IFeatureState<String> status;

    public Feature(String name, Set<String> dependencies, IPath location)
            throws ResolutionException {
        this.name = name;
        this.dependencies = dependencies;
        this.featureLocation = location;
        this.status = IOC.resolve(Keys.getOrAdd("feature-state:initial-state"));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public IPath getFeatureLocation() {
        return this.featureLocation;
    }

    @Override
    public IFeatureState<String> getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(IFeatureState<String> status) {
        this.status = status;
    }
}
