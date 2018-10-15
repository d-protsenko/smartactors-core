package info.smart_tools.smartactors.class_management.interfaces.imodule;


import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.util.Set;

/**
 * Interface IModule
 */
public interface IModule {

    String getName();

    String getVersion();

    Object getId();

    Set<IModule> getDependencies();

    ISmartactorsClassLoader getClassLoader();

    void addDependency(IModule base);

    void setDefault();
}
