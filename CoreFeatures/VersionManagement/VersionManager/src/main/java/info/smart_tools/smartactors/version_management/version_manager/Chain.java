package info.smart_tools.smartactors.version_management.version_manager;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import info.smart_tools.smartactors.version_management.interfaces.imodule.IModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Chain {
    private Object id;
    private List<IResolveDependencyStrategy> versionResolutionStrategies;
    private List<Object> versions;
    private Map<Object, IModule> modules;

    Chain(Object id) {
        this.id = id;
        this.versionResolutionStrategies = Collections.synchronizedList(new LinkedList<>());
        this.versions = Collections.synchronizedList(new LinkedList<>());
        modules = new ConcurrentHashMap<>();
    }

    public String getName() { return name; }

    public String getVersion() { return version; }

    public Object getId() { return id; }

    public Set<IModule> getDependencies() { return dependsOn; }

    public ISmartactorsClassLoader getClassLoader() { return classLoader; }

    public void addDependency(IModule base) {
        if (base != this) {
            dependsOn.add(base);
            dependsOn.addAll(base.getDependencies());
            classLoader.addDependency(base.getClassLoader());
        }
    }

    public void setDefault() { classLoader.setDefault(); }
}
