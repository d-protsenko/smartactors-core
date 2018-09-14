package info.smart_tools.smartactors.class_management.class_loader_management;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link URLClassLoader}
 */
public class HierarchicalClassLoader extends ExtendedURLClassLoader implements ISmartactorsClassLoader {

    /* This is ItemID To ClassLoader Map */
    private static Map<String, HierarchicalClassLoader> itemClassLoaders = new ConcurrentHashMap<>();

    //private String itemName = null;
    private Set<HierarchicalClassLoader> dependsOn = Collections.synchronizedSet(new HashSet<>());

    static void addItem(String itemID) {
        HierarchicalClassLoader classLoader = new HierarchicalClassLoader(new URL[]{});
        itemClassLoaders.put(itemID, classLoader);
    }

    static HierarchicalClassLoader getItemClassLoader(String itemID) {
        return itemClassLoaders.get(itemID);
    }

    static void setItemName(String itemID, String itemName) {
/*        itemName = itemName.replace('/', '.');
        itemName = itemName.replace(':', '.');
        itemName = itemName.replace('-', '_');
        HierarchicalClassLoader classLoader = getItemClassLoader(itemID);
        classLoader.itemName = itemName; */
    }

    static void addItemDependency(String dependentItemID, String baseItemID) {
        if (!baseItemID.equals(dependentItemID)) {
            HierarchicalClassLoader baseClassLoader = getItemClassLoader(baseItemID);
            HierarchicalClassLoader dependentClassLoader = getItemClassLoader(dependentItemID);
            if (baseClassLoader != null && dependentClassLoader != null) {
                dependentClassLoader.dependsOn.add(baseClassLoader);
            }
        }
    }

    static void finalizeItemDependencies(String itemID, String defaultItemID) {
        if (getItemClassLoader(itemID).dependsOn.size() == 0) {
            addItemDependency(itemID, defaultItemID);
        }
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private HierarchicalClassLoader(final URL[] urls) {
        super(urls);
    }

    private void addDependenciesToSet(Set<ClassLoader> classLoaders) {
        classLoaders.add(this);
        if (dependsOn.size() == 0) {
            ClassLoader parent = this.getParent();
            while(parent != null) {
                classLoaders.add(parent);
                parent = parent.getParent();
            }
        } else {
            for(HierarchicalClassLoader classLoader : dependsOn) {
                classLoader.addDependenciesToSet(classLoaders);
            }
        }
    }

    public URL[] getURLsFromDependencies() {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        addDependenciesToSet(classLoaders);

        ArrayList<URL> urlArrayList = new ArrayList<>();
        for( ClassLoader classLoader : classLoaders) {
            if (classLoader instanceof URLClassLoader) {
                Collections.addAll(urlArrayList, ((URLClassLoader) classLoader).getURLs());
            }
        }

        URL[] urls = new URL[urlArrayList.size()];
        urlArrayList.toArray(urls);

        return urls;
    }

    /**
     * Search class in this class loader and each of its dependencies
     * (if dependency is instance of HierarchicalClassLoader)
     * @param className The name of the class to get
     * @return The reference to the class
     */
    private final Class<?> searchClassInDependencies(String className) {
        Class clazz = this.findLoadedClass(className);
        if (null == clazz) {
            for(HierarchicalClassLoader dependency : this.dependsOn) {
                clazz = dependency.searchClassInDependencies(className);
                if (clazz != null) {
                    break;
                }
            }
        }
        return clazz;
    }

    private Class<?> loadClassFromDependencies(String className, ClassLoader scl, ClassLoader ccl, boolean[] rclUsed )
            throws ClassNotFoundException {
        if (dependsOn.size() == 0) {
            try {
                ClassLoader parent = getParent();
                if (parent != scl) {
                    return parent.loadClass(className);
                } else if (!rclUsed[0]) {
                    rclUsed[0] = true;
                    return parent.loadClass(className);
                }
            } catch (ClassNotFoundException e) { }
        } else {
            for (HierarchicalClassLoader dependency : dependsOn) {
                try {
                    if (dependency != ccl) {
                        return dependency.loadClassFromDependencies(className, scl, ccl, rclUsed);
                    } else if (!rclUsed[1]) {
                        rclUsed[1] = true;
                        return dependency.loadClassFromDependencies(className, scl, ccl, rclUsed);
                    }
                } catch (ClassNotFoundException e) { }
            }
        }
        return this.findClass(className);
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {

            Class clazz = this.searchClassInDependencies(className);
            if (clazz == null) {
                boolean[] sclUsed = {false, false};
                clazz = this.loadClassFromDependencies(
                        className,
                        getSystemClassLoader(),
                        itemClassLoaders.get(VersionManager.coreID),
                        sclUsed
                );
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }

    public ClassLoader getCompilationClassLoader() { return this; }
}
