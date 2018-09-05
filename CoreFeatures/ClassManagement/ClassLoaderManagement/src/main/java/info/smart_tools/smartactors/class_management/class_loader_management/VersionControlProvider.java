package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

/**
 * Class VersionControlProvider
 */
public final class VersionControlProvider {

    public static final String coreID = java.util.UUID.randomUUID().toString(); // "core-feature-id";
    public static final String coreName = "info.smart_tools.smartactors";

    private VersionControlProvider() {}

    public static void addItem(String itemID) {
        ExtendedURLClassLoader.addItem(itemID);
    }

    public static void setItemName(String itemID, String itemName) {
        ExtendedURLClassLoader.setItemName(itemID, itemName);
    }

    public static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return (ISmartactorsClassLoader)ExtendedURLClassLoader.getItemClassLoader(itemID);
    }

    /*public static void setCurrentItem(String itemID) {
        ExtendedURLClassLoader.setCurrentItem(itemID);
    }*/

    public static void addItemDependency(String dependentItemID, String baseItemID) {
        ExtendedURLClassLoader.addItemDependency(dependentItemID, baseItemID);
    }

    public static void finalizeItemDependencies(String itemID, String defaultItemID) {
        ExtendedURLClassLoader.finalizeItemDependencies(itemID, defaultItemID);
    }
}
