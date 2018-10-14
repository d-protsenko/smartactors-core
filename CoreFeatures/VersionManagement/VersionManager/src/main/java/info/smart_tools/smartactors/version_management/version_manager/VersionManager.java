package info.smart_tools.smartactors.version_management.version_manager;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.version_management.interfaces.imodule.IModule;
import info.smart_tools.smartactors.version_management.version_manager.exception.VersionManagerException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class VersionManager
 */
public final class VersionManager {

    public  static final Object coreId = java.util.UUID.randomUUID();
    private static final String coreName = "info.smart_tools:smartactors";
    private static final String coreVersion = "0.4.0";

    private static ThreadLocal<IModule> currentModule = new ThreadLocal<>();
    private static ThreadLocal<IObject> currentMessage = new ThreadLocal<>();

    private static Map<Object, IModule> modules = new ConcurrentHashMap<>();

    static {
        try {
            addModule(coreId, coreName, coreVersion);
            modules.get(coreId).setDefault();
        } catch(InvalidArgumentException | VersionManagerException e) {}
    }

    private VersionManager() {}

    public static void addModule(final Object moduleId, final String moduleName, final String moduleVersion)
            throws InvalidArgumentException, VersionManagerException {
        if (moduleId == null || moduleName == null || moduleName.equals("")) {
            throw new InvalidArgumentException("Module id or name cannot be null.");
        }

        String version = (moduleVersion == null ? "" : moduleVersion);

        if (modules.get(moduleId) == null) {
            modules.put(moduleId, new Module(moduleId, moduleName, version));
        } else {
            throw new VersionManagerException("Module "+String.valueOf(moduleId)+" already defined.");
        }
    }

    public static ISmartactorsClassLoader getCurrentClassLoader() {
        return currentModule.get().getClassLoader();
    }

    public static void addModuleDependency(Object dependentModuleId, Object baseModuleId)
            throws InvalidArgumentException {

        if (baseModuleId == null || dependentModuleId == null) {
            throw new InvalidArgumentException("Module id cannot be null.");
        }

        modules.get(dependentModuleId).addDependency(modules.get(baseModuleId));
    }

    public static void finalizeModuleDependencies(Object moduleId)
            throws InvalidArgumentException {
        if (moduleId == null) {
            throw new InvalidArgumentException("Module id cannot be null.");
        }

        if (modules.get(moduleId).getDependencies().size() == 0) {
            modules.get(moduleId).addDependency(modules.get(coreId));
        }
    }

    public static IModule getModuleById(Object moduleId) { return modules.get(moduleId); }

    static IObject getCurrentMessage() { return currentMessage.get(); }

    public static void setCurrentMessage(IObject message) { currentMessage.set(message); }

    public static void setCurrentModule(IModule module) { currentModule.set(module); }

    public static IModule getCurrentModule() {
        return currentModule.get();
    }

    public static <T> T getFromMap(IModule module, Map<IModule, T> objects) {
        T object = objects.get(module);
        if (object == null) {
            for(Object dependency : module.getDependencies()) {
                object = objects.get(dependency);
                if (object != null) {
                    break;
                }
            }
        }
        return object;
    }

    public static <T> T getFromMap(Map<IModule, T> objects) {
        return getFromMap(getCurrentModule(), objects);
    }

    public static <T> T removeFromMap(IModule module, Map<IModule, T> objects) {
        T object = objects.remove(module);
        if (object == null) {
            for(Object dependency : module.getDependencies()) {
                object = objects.remove(dependency);
                if (object != null) {
                    break;
                }
            }
        }
        return object;
    }

    public static <T> T removeFromMap(Map<IModule, T> objects) {
        return removeFromMap(getCurrentModule(), objects);
    }

}
