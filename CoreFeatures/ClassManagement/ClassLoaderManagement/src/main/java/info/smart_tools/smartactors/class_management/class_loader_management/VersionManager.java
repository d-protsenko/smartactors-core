package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.util.Map;
import java.util.Set;
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

    private VersionManager() {}

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
