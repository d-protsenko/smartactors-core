package info.smart_tools.smartactors.class_management.version_manager;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IFunction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.class_management.hierarchical_class_loader.SmartactorsClassLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class VersionManager
 */
public final class VersionManager {

    public static final String coreName = "info.smart_tools:smartactors";
    public static final String coreVersion = "0.4.1";
    public static final Object coreId = java.util.UUID.randomUUID();
    private static Object defaultModuleID = null;

    private static ThreadLocal<Object> currentModuleId = new ThreadLocal<>();
    private static ThreadLocal<IObject> currentMessage = new ThreadLocal<>();

    private static Map<Object, Set<Object>> dependencies = new ConcurrentHashMap<>();
    private static Map<Object, String> moduleNames = new ConcurrentHashMap<>();
    private static Map<Object, String> moduleVersions = new ConcurrentHashMap<>();

    private static Map<Object, List<IResolveDependencyStrategy>> versionStrategies = new ConcurrentHashMap<>();
    private static Map<Object, List<Object>> chainVersions = new ConcurrentHashMap<>();
    private static Map<Object, Map<Object, Object>> chainModuleIds = new ConcurrentHashMap<>();

    private VersionManager() {}

    public static void addModule(final Object moduleId, final String moduleName, final String moduleVersion)
            throws InvalidArgumentException {
        if (moduleName == null || moduleName.equals("")) {
            throw new InvalidArgumentException("Item name cannot be null");
        }

        String version = (moduleVersion == null ? "" : moduleVersion);

        if (dependencies.get(moduleId) == null) {
            SmartactorsClassLoader.addModule(moduleId, moduleName, version);
            dependencies.put(moduleId, Collections.synchronizedSet(new HashSet<>()));
            moduleNames.put(moduleId, moduleName);
            moduleVersions.put(moduleId, version);
        } else {
            System.out.println("[WARNING] Module "+moduleId+" already defined.\n");
        }
    }

    public static void setDefaultModuleId(final Object moduleId) {
        SmartactorsClassLoader.setDefaultModuleId(moduleId);
        defaultModuleID = moduleId;
    }

    private static String getModuleName(Object moduleId) {
        return moduleNames.get(moduleId);
    }

    private static String getModuleVersion(Object moduleId) {
        return moduleVersions.get(moduleId);
    }

    public static ISmartactorsClassLoader getModuleClassLoader(Object moduleId) {
        return SmartactorsClassLoader.getModuleClassLoader(moduleId);
    }

    public static void addModuleDependency(Object dependentModuleId, Object baseModuleId) {
        if (baseModuleId != null && dependentModuleId != null && dependentModuleId != baseModuleId) {
            Set<Object> moduleIDs = dependencies.get(baseModuleId);
            Set<Object> dependsOn = dependencies.get(dependentModuleId);
            // it is based on ConcurrentHashMap, so it is thread safe
            dependsOn.add(baseModuleId);
            dependsOn.addAll(moduleIDs);

            SmartactorsClassLoader.addModuleDependency(dependentModuleId, baseModuleId);
        }
    }

    public static void finalizeModuleDependencies(Object moduleId) {
        if (moduleId != null) {
            SmartactorsClassLoader.finalizeModuleDependencies(moduleId);
            Set<Object> dependsOn = dependencies.get(moduleId);
            if (dependsOn.size() == 0 && defaultModuleID != null) {
                dependsOn.add(defaultModuleID);
            }
        }
    }

    public static void setCurrentMessage(IObject message) { currentMessage.set(message); }

    public static IObject getCurrentMessage() { return currentMessage.get(); }

    public static void setCurrentModule(Object moduleId) {
        currentModuleId.set(moduleId);
    }

    public static Object getCurrentModule() {
        return currentModuleId.get();
    }

    public static <T> T getFromMap(Object moduleId, Map<Object, T> objects) {
        T object = objects.get(moduleId);
        if (object == null) {
            for(Object dependency : dependencies.get(moduleId)) {
                object = objects.get(dependency);
                if (object != null) {
                    break;
                }
            }
        }
        return object;
    }

    public static <T> T getFromMap(Map<Object, T> objects) {
        return getFromMap(getCurrentModule(), objects);
    }

    public static <T> T removeFromMap(Object moduleId, Map<Object, T> objects) {
        T object = objects.remove(moduleId);
        if (object == null) {
            for(Object dependency : dependencies.get(moduleId)) {
                object = objects.remove(dependency);
                if (object != null) {
                    break;
                }
            }
        }
        return object;
    }

    public static <T> T removeFromMap(Map<Object, T> objects) {
        return removeFromMap(getCurrentModule(), objects);
    }

    public static void registerChain(Object chainId)
            throws InvalidArgumentException {
        Object moduleID = getCurrentModule();
        Object version = moduleVersions.get(moduleID);
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, Object> moduleVersions = chainModuleIds.get(chainId);
        if (moduleVersions == null) {
            moduleVersions = new ConcurrentHashMap<>();
            chainModuleIds.put(chainId, moduleVersions);
        }
        Object previous = moduleVersions.put(version, moduleID);
        if (previous != null) {
            System.out.println(
                    "[WARNING] Replacing chain "+chainId+":"+version+" registered from feature "+
                            getModuleName(previous)+":"+ getModuleVersion(previous)+" by chain from feature "+
                            getModuleName(moduleID)+":"+ getModuleVersion(moduleID)
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
        Object version = moduleVersions.get(getCurrentModule());
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

    public static <T> T applyVersionResolutionStrategy(final Object ... args)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        String chainID = (String)args[0];
        if (chainID == null) {
            throw new InvalidArgumentException("Key for version resolution strategy cannot be null.");
        }
        List<IResolveDependencyStrategy> strategies = versionStrategies.get(chainID);
        if (strategies == null) {
            throw new ResolveDependencyStrategyException("Key '"+chainID+"' is not registered.");
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

    private static Object getModuleIdByChainVersion(final Object chainId, final Object version)
            throws InvalidArgumentException {
        if (chainId == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, Object> versions = chainModuleIds.get(chainId);
        if (versions == null) {
            return null;
        }
        return versions.get(version);
    }

    public static Object getModuleIdByChainId(Object chainId)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        Object moduleID = getCurrentModule();
        IObject context = getCurrentMessage();
        if (context != null || moduleID == null) {
            Object chainVersion = VersionManager.applyVersionResolutionStrategy(chainId, context);
            moduleID = VersionManager.getModuleIdByChainVersion(chainId, chainVersion);
        }
        return moduleID;
    }
}
