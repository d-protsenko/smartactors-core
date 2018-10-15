package info.smart_tools.smartactors.class_management.module_manager;

import info.smart_tools.smartactors.class_management.hierarchical_class_loader.SmartactorsClassLoader;
import info.smart_tools.smartactors.class_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Module implements IModule {
    private String name;
    private String version;
    private Object id;
    private Set<IModule> dependsOn;
    private ISmartactorsClassLoader classLoader;

    Module(Object id, String name, String version) {
        this.name = name;
        this.version = version;
        this.id = id;
        this.dependsOn = Collections.synchronizedSet(new HashSet<>());
        this.classLoader = SmartactorsClassLoader.newInstance(name, version);
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
