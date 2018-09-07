package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

/**
 * Class VersionControlProvider
 */
public final class VersionControlProvider {

    public static final String coreID = java.util.UUID.randomUUID().toString(); // "core-feature-id";
    public static final String coreName = "info.smart_tools.smartactors";
    private static ThreadLocal<String> currentItemID = new ThreadLocal<String>();


    private VersionControlProvider() {}

    public static void addItem(String itemID) {
        HierarchicalClassLoader.addItem(itemID);
    }

    public static void setItemName(String itemID, String itemName) {
        HierarchicalClassLoader.setItemName(itemID, itemName);
    }

    public static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return (ISmartactorsClassLoader)HierarchicalClassLoader.getItemClassLoader(itemID);
    }

    public static void addItemDependency(String dependentItemID, String baseItemID) {
        HierarchicalClassLoader.addItemDependency(dependentItemID, baseItemID);
    }

    public static void finalizeItemDependencies(String itemID, String defaultItemID) {
        HierarchicalClassLoader.finalizeItemDependencies(itemID, defaultItemID);
    }

    public static void setCurrentItemID(String itemID) {
        currentItemID.set(itemID);
    }

    public static String getCurrentItemID() {
        return currentItemID.get();
    }
}
