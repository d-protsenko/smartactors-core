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
        SmartactorsClassLoader.addItem(itemID);
    }

    public static void setItemName(String itemID, String itemName) {
        SmartactorsClassLoader.setItemName(itemID, itemName);
    }

    public static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return SmartactorsClassLoader.getItemClassLoader(itemID);
    }

    /*public static void setCurrentItem(String itemID) {
        SmartactorsClassLoader.setCurrentItem(itemID);
    }*/

    public static void addItemDependency(String itemIdDependent, String itemIdOnWhichDepend) {
        SmartactorsClassLoader.addItemDependency(itemIdDependent, itemIdOnWhichDepend);
    }

    public static void finalizeItemDependencies(String itemID, String defaultItemID) {
        SmartactorsClassLoader.finalizeItemDependencies(itemID, defaultItemID);
    }
}
