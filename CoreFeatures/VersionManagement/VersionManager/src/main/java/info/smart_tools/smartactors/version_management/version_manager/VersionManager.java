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
    private static Map<Object, Chain> chains = new ConcurrentHashMap<>();

    private static Map<Object, List<IResolveDependencyStrategy>> versionStrategies = new ConcurrentHashMap<>();
    private static Map<Object, List<Object>> chainVersions = new ConcurrentHashMap<>();
    private static Map<Object, Map<Object, IModule>> chainModuleIds = new ConcurrentHashMap<>();

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
        Object version = module.getVersion();

        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Id and version of chain cannot be null.");
        }

        Chain chain = chains.get(chainId);
        if (chain == null) {
            chain = new Chain(chainId);
            chains.put(chainId, chain);
        }
        chain.addVersion(version, module)



//-----------
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, IModule> moduleVersions = chainModuleIds.get(chainId);
        if (moduleVersions == null) {
            moduleVersions = new ConcurrentHashMap<>();
            chainModuleIds.put(chainId, moduleVersions);
        }
        IModule previous = moduleVersions.put(version, module);
        if (previous != null) {
            System.out.println(
                    "[WARNING] Replacing chain "+chainId+":"+version+" registered from feature "+
                            previous.getName()+":"+previous.getVersion()+" by chain from feature "+
                            module.getName()+":"+ module.getVersion()
            );
        } else {
            List<Object> versions = chainVersions.get(chainId);
            if (versions == null || !versions.contains(version)) {
                registerVersionResolutionStrategy(chainId, null);
            }
        }
    }

    public static void registerVersionResolutionStrategy(Object chainId, IResolveDependencyStrategy strategy)
            throws InvalidArgumentException {
        Object version = getCurrentModule().getVersion();
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        List<Object> versions = chainVersions.get(chainId);
        List<IResolveDependencyStrategy> strategies = versionStrategies.get(chainId);
        if (versions == null || strategies == null) {
            versions = Collections.synchronizedList(new LinkedList<>());
            chainVersions.put(chainId, versions);
            strategies = Collections.synchronizedList(new LinkedList<>());
            versionStrategies.put(chainId, strategies);
        }
        int i;
        for(i = 0; i < versions.size(); i++) {
            int res = String.valueOf(versions.get(i)).compareTo(String.valueOf(version));
            if (res == 0) {
                versions.remove(i);
                strategies.remove(i);
                break;
            }
            if (res < 0 ) {
                break;
            }
        }
        versions.add(i, version);
        strategies.add(i, strategy);
    }

    private static <T> T applyVersionResolutionStrategy(final Object ... args)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        String chainID = String.valueOf(args[0]);
        if (chainID == null) {
            throw new InvalidArgumentException("Key for version resolution strategy cannot be null.");
        }
        List<IResolveDependencyStrategy> strategies = versionStrategies.get(chainID);
        if (strategies == null) {
            throw new ResolveDependencyStrategyException("Version resolution strategy container for '"+chainID+"' is not registered.");
        }
        T result = null;
        for(IResolveDependencyStrategy strategy : strategies) {
            if (strategy != null) {
                result = strategy.resolve(args);
                if (result != null) {
                    return result;
                }
            }
        }
        // default strategy is to return max version
        return (T)chainVersions.get(chainID).get(0);
    }

    private static IModule getModuleByChainVersion(final Object chainId, final Object version)
            throws InvalidArgumentException {
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, IModule> versions = chainModuleIds.get(chainId);
        if (versions == null) {
            return null;
        }
        return versions.get(version);
    }

    public static IModule getModuleByChainId(Object chainId)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        IModule module = getCurrentModule();
        IObject context = getCurrentMessage();
        if (context != null || module == null) {
            Object chainVersion = VersionManager.applyVersionResolutionStrategy(chainId, context);
            module = VersionManager.getModuleByChainVersion(chainId, chainVersion);
            if (module == null) {
                throw new ResolveDependencyStrategyException(
                        "Resolution failed for chain '"+String.valueOf(chainId)+":"+String.valueOf(chainVersion)+"'."
                );
            }
        }
        return module;
    }
}
