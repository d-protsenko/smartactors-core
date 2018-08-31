package info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader;

import java.net.URL;
import java.util.List;

/**
 * Interface ISmartactorsClassLoader
 */
public interface ISmartactorsClassLoader {

    static void addItem(String itemID, String itemName) {}

    static void addItemDependency(String itemIdDependent, String itemIdOnWhichDepend) {}

    static void finalizeItemDependencies(String itemId, String defaultItemID) {}

    static void setCurrentItem(String itemID) {}

    static ISmartactorsClassLoader getItemClassLoader(String itemID) { return null; }

    /**
     * Add new dependency on {@link ISmartactorsClassLoader} to this {@link ISmartactorsClassLoader}
     * @param classLoader {@link ISmartactorsClassLoader} which this {@link ISmartactorsClassLoader} depends on
     */
    void addDependency(ISmartactorsClassLoader classLoader);

    /**
     * Get list of dependencies for this {@link ISmartactorsClassLoader}
     * @return list of ISmartactorsClassLoader items which this class loader depends on
     */
    List<ISmartactorsClassLoader> getDependencies();

    /**
     * Add {@link URL} to the current url class loader if url class loader doesn't contain this {@link URL} yet
     * @param url instance of {@link URL}
     */
    void addUrl(final URL url);

    Class<?> loadClass(String className) throws ClassNotFoundException;
}
