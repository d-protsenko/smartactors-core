package info.smart_tools.smartactors.class_management.class_loader_management;


import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

/**
 * Class VersionControlProvider
 */
public final class VersionControlProvider  {

    private VersionControlProvider() {}

    public static void addItem(String itemID, String itemName) {
        SmartactorsClassLoader.addItem(itemID, itemName);
    }

    public static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return SmartactorsClassLoader.getItemClassLoader(itemID);
    }

    public static void setCurrentItem(String itemID) {
        SmartactorsClassLoader.setCurrentItem(itemID);
    }

    public static void addItemDependency(String itemIdDependent, String itemIdOnWhichDepend) {
        SmartactorsClassLoader.addItemDependency(itemIdDependent, itemIdOnWhichDepend);
    }

    public static void finalizeItemDependencies(String itemID, String defaultItemID) {
        SmartactorsClassLoader.finalizeItemDependencies(itemID, defaultItemID);
    }
}
