package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import info.smart_tools.smartactors.iobject.iobject.IObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class VersionManager
 */
public final class VersionManager {

    public static final String coreName = "info.smart_tools:smartactors";
    public static final String coreVersion = "0.4.1";
    public static final Object coreId = java.util.UUID.randomUUID();

    private static ThreadLocal<Object> currentItemID = new ThreadLocal<>();
    private static ThreadLocal<IObject> currentMessage = new ThreadLocal<>();

    private static Map<Object, Set<Object>> dependencies = new ConcurrentHashMap<>();
    private static Map<Object, String> itemNames = new ConcurrentHashMap<>();
    private static Map<Object, String> itemVersions = new ConcurrentHashMap<>();

    private static Map<Object, List<IResolveDependencyStrategy>> versionStrategies = new ConcurrentHashMap<>();
    private static Map<Object, List<Object>> chainVersions = new ConcurrentHashMap<>();
    private static Map<Object, Map<Object, Object>> chainItemIDs = new ConcurrentHashMap<>();

    private VersionManager() {}

    public static void addItem(final Object itemId, final String itemName, final String itemVersion)
            throws InvalidArgumentException {
        if (itemName == null || itemName.equals("")) {
            throw new InvalidArgumentException("Item name cannot be null");
        }

        String version = (itemVersion == null ? "" : itemVersion);

        if (dependencies.get(itemId) == null) {
            HierarchicalClassLoader.addItem(itemId, itemName, version);
            dependencies.put(itemId, Collections.synchronizedSet(new HashSet<>()));
            itemNames.put(itemId, itemName);
            itemVersions.put(itemId, version);
        } else {
            System.out.println("[WARNING] Item "+itemId+" already defined.\n");
        }
    }

    private static String getItemName(Object itemID) {
        return itemNames.get(itemID);
    }

    private static String getItemVersion(Object itemID) {
        return itemVersions.get(itemID);
    }

    public static ISmartactorsClassLoader getItemClassLoader(Object itemID) {
        return HierarchicalClassLoader.getItemClassLoader(itemID);
    }

    public static void addItemDependency(Object dependentItemID, Object baseItemID) {
        if (baseItemID != null && dependentItemID != null && dependentItemID != baseItemID) {
            Set<Object> itemIDs = dependencies.get(baseItemID);
            Set<Object> dependsOn = dependencies.get(dependentItemID);
            // it is based on ConcurrentHashMap, so it is thread safe
            dependsOn.add(baseItemID);
            dependsOn.addAll(itemIDs);

            HierarchicalClassLoader.addItemDependency(dependentItemID, baseItemID);
        }
    }

    public static void finalizeItemDependencies(Object itemID, Object defaultItemID) {
        if (itemID != null) {
            HierarchicalClassLoader.finalizeItemDependencies(itemID, defaultItemID);
            Set<Object> dependsOn = dependencies.get(itemID);
            if (dependsOn.size() == 0 && defaultItemID != null) {
                dependsOn.add(defaultItemID);
            }
        }
    }

    public static void setCurrentMessage(IObject message) { currentMessage.set(message); }

    public static IObject getCurrentMessage() { return currentMessage.get(); }

    public static void setCurrentModule(Object itemID) {
        currentItemID.set(itemID);
    }

    public static Object getCurrentModule() {
        return currentItemID.get();
    }

    public static <T> T getFromMap(Object itemID, Map<Object, T> objects) {
        T object = objects.get(itemID);
        if (object == null) {
            for(Object dependency : dependencies.get(itemID)) {
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

    public static <T> T removeFromMap(Object itemID, Map<Object, T> objects) {
        T object = objects.remove(itemID);
        if (object == null) {
            for(Object dependency : dependencies.get(itemID)) {
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

    public static void registerChainVersion(Object chainID)
            throws InvalidArgumentException {

        Object itemID = getCurrentModule();
        Object version = itemVersions.get(itemID);
        if (chainID == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, Object> versions = chainItemIDs.get(chainID);
        if (versions == null) {
            versions = new ConcurrentHashMap<>();
            chainItemIDs.put(chainID, versions);
        }
        Object previous = versions.put(version, itemID);
        if (null != previous) {
            System.out.println(
                    "[WARNING] Replacing chain "+chainID+"-"+version+" registered from feature "+
                            getItemName(previous)+"-"+getItemVersion(previous)+" by chain from feature "+
                            getItemName(itemID)+"-"+getItemVersion(itemID)
            );
            versionStrategies.put(chainID, Collections.synchronizedList(new LinkedList<>()));
            chainVersions.put(chainID, Collections.synchronizedList(new LinkedList<>()));
        }
        registerVersionResolutionStrategy(chainID, version, new ApplyFunctionToArgumentsStrategy(args -> {
            Object argumentChainID = args[0];
            return chainVersions.get(argumentChainID).get(0);
        }));
    }

    public static void registerVersionResolutionStrategy(Object chainID, Object version, IResolveDependencyStrategy strategy)
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
        List<Object> versions = chainVersions.get(chainID);
        if (versions == null) {
            versions = Collections.synchronizedList(new LinkedList<>());
            chainVersions.put(chainID, versions);
        }
        int i;
        for(i = 0; i < versions.size(); i++) {
            int res = String.valueOf(versions.get(i)).compareTo(String.valueOf(version));
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

    public static Object getItemIDByChainVersion(final Object chainID, final Object version)
            throws InvalidArgumentException {
        if (chainID == null || version == null) {
            throw new InvalidArgumentException("Key and version of chain cannot be null.");
        }
        Map<Object, Object> versions = chainItemIDs.get(chainID);
        if (versions == null) {
            return null;
        }
        return versions.get(version);
    }

    public static Object getItemIDByChainID(Object chainID)
            throws InvalidArgumentException, ResolveDependencyStrategyException {
        Object itemID = getCurrentModule();
        IObject context = getCurrentMessage();
        if (context != null || itemID == null) {
            Object chainVersion = VersionManager.applyVersionResolutionStrategy(chainID, context);
            itemID = VersionManager.getItemIDByChainVersion(chainID, chainVersion);
        }
        return itemID;
    }
}
