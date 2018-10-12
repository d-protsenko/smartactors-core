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
    private static Map<Object, ChainVersionStrategies> chainVersionStrategies = new ConcurrentHashMap<>();
    private static Map<Object, Map<String, IModule>> chainVersionModules = new ConcurrentHashMap<>();

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

    public static void registerChain(Object chainId)
            throws InvalidArgumentException {
        IModule module = getCurrentModule();
        String version = module.getVersion();

        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Chain id or version cannot be null.");
        }

        Map<String, IModule> versions = chainVersionModules.get(chainId);
        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            chainVersionModules.put(chainId, versions);
        }

        IModule previous = versions.put(version, module);
        if (previous != null) {
            System.out.println(
                    "[WARNING] Replacing chain " + chainId + ":" + version + " registered from feature " +
                            previous.getName() + ":" + previous.getVersion() + " by chain from feature " +
                            module.getName() + ":" + module.getVersion()
            );
        } else {
            registerVersionResolutionStrategy(chainId, null);
        }
  }

    public static void registerVersionResolutionStrategy(Object chainId, IResolveDependencyStrategy strategy)
            throws InvalidArgumentException {
        String version = getCurrentModule().getVersion();
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        ChainVersionStrategies versionStrategies = chainVersionStrategies.get(chainId);
        if (versionStrategies == null) {
            versionStrategies = new ChainVersionStrategies(chainId);
            chainVersionStrategies.put(chainId, versionStrategies);
        }
        versionStrategies.registerVersionResolutionStrategy(version, strategy);
    }

    private static String getVersionByChainId(Object chainId)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        if (chainId == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        ChainVersionStrategies versionStrategies = chainVersionStrategies.get(chainId);
        if (versionStrategies == null) {
            throw new ResolveDependencyStrategyException("Chain with id '"+String.valueOf(chainId)+"' is not registered.");
        }
        IObject message = getCurrentMessage();
        if (message == null) {
            throw new ResolveDependencyStrategyException("Cannot resolve chain version on null message.");
        }
        return versionStrategies.resolveVersion(message);
    }

    public static IModule getModuleByChainId(Object chainId)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        if (chainId == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        if (getCurrentMessage() == null) {
            return getCurrentModule();
        }
        String version = getVersionByChainId(chainId);
        Map<String, IModule> versions = chainVersionModules.get(chainId);
        if (versions == null) {
            throw new ResolveDependencyStrategyException("Chain with id '"+String.valueOf(chainId)+"' is not registered.");
        }
        IModule module = versions.get(version);
        if (module == null) {
            throw new ResolveDependencyStrategyException("Resolution failed for chain '"+String.valueOf(chainId)+"'.");
        }
        return module;
    }
}
