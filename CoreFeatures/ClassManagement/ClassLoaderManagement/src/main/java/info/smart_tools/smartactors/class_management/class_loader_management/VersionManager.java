package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class VersionManager
 */
public final class VersionManager {

    public static final String coreName = "info.smart_tools.smartactors";
    public static final String coreVersion = " 0.4.0";
    public static final String coreID = coreName+coreVersion;
    private static ThreadLocal<String> currentItemID = new ThreadLocal<String>();

    private static Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();
    private static Map<String, String> itemNames = new ConcurrentHashMap<>();
    private static Map<String, String> itemVersions = new ConcurrentHashMap<>();

    private static Map<String, List<IResolveDependencyStrategy>> versionStrategies = new ConcurrentHashMap<>();
    private static Map<String, List<String>> chainVersions = new ConcurrentHashMap<>();
    private static Map<String, Map<String, String>> chainItemIDs = new ConcurrentHashMap<>();

    private VersionManager() {}

    public static void registerVersionResolutionStrategy(String chainID, String version, IResolveDependencyStrategy strategy)
            throws InvalidArgumentException {
        if (chainID == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        if (strategy == null) {
            throw new InvalidArgumentException("Cannot register null version resolution strategy.");
        }
        List<IResolveDependencyStrategy> strategies = versionStrategies.get(chainID);
        if (strategies == null) {
            strategies = Collections.synchronizedList(new LinkedList<>());
            versionStrategies.put(chainID, strategies);
        }
        List<String> versions = chainVersions.get(chainID);
        if (versions == null) {
            versions = Collections.synchronizedList(new LinkedList<>());
            chainVersions.put(chainID, versions);
        }
        int i;
        for(i = 0; i < versions.size(); i++) {
            int res = versions.get(i).compareTo(version);
            if (res == 0) {
                versions.remove(i);
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
            result = strategy.resolve(args);
            if (result != null) {
                return result;
            }
        }
        throw new ResolveDependencyStrategyException("All strategies failed while resolving key '"+chainID+"'.");
    }

    public static void registerChainVersion(String key, String version, String itemID)
            throws InvalidArgumentException {

        if (key == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<String, String> versions = chainItemIDs.get(key);
        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            chainItemIDs.put(key, versions);
        }
        String previous = versions.put(version, itemID);
        if (null != previous) {
            System.out.println(
                "[WARNING] Replacing chain "+key+"-"+version+" registered from feature "+
                getItemName(previous)+"-"+getItemVersion(previous)+" by chain from feature "+
                getItemName(itemID)+"-"+getItemVersion(itemID)
            );
            versionStrategies.put(key, Collections.synchronizedList(new LinkedList<>()));
            chainVersions.put(key, Collections.synchronizedList(new LinkedList<>());
        }
        registerVersionResolutionStrategy(key, version, new ApplyFunctionToArgumentsStrategy(args -> {
            String chainID = (String) args[0];
            return chainVersions.get(chainID).get(0);
        }));
    }

    public static String getItemIDByChainVersion(final String chainID, final String version)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        if (chainID == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<String, String> versions = chainItemIDs.get(chainID);
        if (versions == null) {
            return null;
        }
        return versions.get(version);
    }

    public static void addItem(String itemID) {
        if (dependencies.get(itemID) == null) {
            HierarchicalClassLoader.addItem(itemID);
            dependencies.put(itemID, (new ConcurrentHashMap<>()).newKeySet());
        }
    }

    public static void setItemName(String itemID, String itemName) {
        HierarchicalClassLoader.setItemName(itemID, itemName);
        itemNames.put(itemID, itemName);
    }

    public static void setItemVersion(String itemID, String itemVersion) {
        itemVersions.put(itemID, itemVersion);
    }

    public static String getItemName(String itemID) {
        return itemNames.get(itemID);
    }

    public static String getItemVersion(String itemID) {
        return itemVersions.get(itemID);
    }

    public static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return HierarchicalClassLoader.getItemClassLoader(itemID);
    }

    public static void addItemDependency(String dependentItemID, String baseItemID) {
        if (baseItemID != null && dependentItemID != null && dependentItemID != baseItemID) {
            Set<String> itemIDs = dependencies.get(baseItemID);
            Set<String> dependsOn = dependencies.get(dependentItemID);
            // it is based on ConcurrentHashMap, so it is thread safe
            dependsOn.add(baseItemID);
            dependsOn.addAll(itemIDs);

            HierarchicalClassLoader.addItemDependency(dependentItemID, baseItemID);
        }
    }

    public static void finalizeItemDependencies(String itemID, String defaultItemID) {
        if (itemID != null) {
            HierarchicalClassLoader.finalizeItemDependencies(itemID, defaultItemID);
            Set<String> dependsOn = dependencies.get(itemID);
            if (dependsOn.size() == 0 && defaultItemID != null) {
                dependsOn.add(defaultItemID);
            }
        }
    }

    public static void setCurrentItemID(String itemID) {
        currentItemID.set(itemID);
    }

    public static String getCurrentItemID() {
        return currentItemID.get();
    }

    public static String getCurrentItemName() {
        return itemNames.get(getCurrentItemID());
    }

    public static String getCurrentItemVersion() {
        return itemVersions.get(getCurrentItemID());
    }

    public static <T> T getFromMap(String itemID, Map<String, T> objects) {
        T object = objects.get(itemID);
        if (object == null) {
            for(String dependency : dependencies.get(itemID)) {
                object = objects.get(dependency);
                if (object != null) {
                    break;
                }
            }
        }
        return object;
    }

    public static <T> T getFromMap(Map<String, T> objects) {
        return getFromMap(getCurrentItemID(), objects);
    }
}
