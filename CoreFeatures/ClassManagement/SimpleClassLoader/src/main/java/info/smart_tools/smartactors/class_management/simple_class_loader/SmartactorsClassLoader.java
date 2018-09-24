package info.smart_tools.smartactors.class_management.simple_class_loader;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extension of {@link URLClassLoader}
 */
public class SmartactorsClassLoader extends URLClassLoader implements ISmartactorsClassLoader {

    private static SmartactorsClassLoader single = null;
    private static SmartactorsClassLoader compilation = null;

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    SmartactorsClassLoader(final URL[] urls) { super(urls); }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private SmartactorsClassLoader(final URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    static void addItem(Object itemID, String itemName, String itemVersion) {
        if (single == null) {
            single = new SmartactorsClassLoader(new URL[]{});
        }
        if (compilation == null) {
            compilation = new SmartactorsClassLoader(new URL[]{}, single);
        }
    }

    static SmartactorsClassLoader getItemClassLoader(Object itemID) {
        return single;
    }

    static void addItemDependency(Object dependentItemID, Object baseItemID) {
    }

    static void finalizeItemDependencies(Object itemID, Object defaultItemID) {
    }

    /**
     * Add new instance of {@link URL} to the current url class loader if url class loader doesn't contain this instance of {@link URL}
     * @param url instance of {@link URL}
     */
    public void addURL(final URL url) {
        super.addURL(url);
    }

    public URL[] getURLsFromDependencies() {

        ArrayList<URL> urlArrayList = new ArrayList<>();
        Collections.addAll(urlArrayList, getURLs());
        ClassLoader parent = getParent();
        while(parent != null) {
            if (parent instanceof URLClassLoader) {
                Collections.addAll(urlArrayList, ((URLClassLoader) parent).getURLs());
            }
            parent = parent.getParent();
        }

        URL[] urls = new URL[urlArrayList.size()];
        urlArrayList.toArray(urls);

        return urls;
    }

    /**
     * Add compiled byte code of the class directly to this class loader
     * @param className The name of the class to define
     * @param classByteCode Compiled byte code of the class to add
     * @return The reference to the class
     */
    public Class<?> addClass(final String className, byte[] classByteCode) {
        return defineClass(className, classByteCode, 0, classByteCode.length);
    }

    public ClassLoader getCompilationClassLoader() { return compilation; }
}
