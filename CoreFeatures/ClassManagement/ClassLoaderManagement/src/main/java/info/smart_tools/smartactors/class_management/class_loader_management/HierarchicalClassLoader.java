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
    private static Map<String, HierarchicalClassLoader> itemClassLoaders =
            new ConcurrentHashMap<String, HierarchicalClassLoader>();

    private String itemName = null;
    private ArrayList<ClassLoader> dependsOn = new ArrayList<ClassLoader>();

    static void addItem(String itemID) {
        HierarchicalClassLoader classLoader = new HierarchicalClassLoader(new URL[]{});
        itemClassLoaders.put(itemID, classLoader);
        if (itemID.equals(VersionControlProvider.coreID)) {
            classLoader.dependsOn.add(classLoader.getParent());
        }
    }

    static HierarchicalClassLoader getItemClassLoader(String itemID) {
        return itemClassLoaders.get(itemID);
    }

    static void setItemName(String itemID, String itemName) {
        itemName = itemName.replace('/', '.');
        itemName = itemName.replace(':', '.');
        itemName = itemName.replace('-', '_');
        HierarchicalClassLoader classLoader = getItemClassLoader(itemID);
        classLoader.itemName = itemName;
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

    private void addParentsToSet(ClassLoader classLoader, Set<ClassLoader> classLoaders) {
        ClassLoader parent = classLoader.getParent();
        while(parent != null) {
            classLoaders.add(parent);
            parent = parent.getParent();
        }
    }

    private void addDependenciesToSet(Set<ClassLoader> classLoaders) {
        if (dependsOn.size() == 0) {
            addParentsToSet(this, classLoaders);
        } else {
            for(ClassLoader classLoader : dependsOn) {
                classLoaders.add(classLoader);
                if (classLoader instanceof HierarchicalClassLoader) {
                    ((HierarchicalClassLoader) classLoader).addDependenciesToSet(classLoaders);
                } else {
                    addParentsToSet(classLoader, classLoaders);
                }
            }
        }
    }

    public URL[] getURLsFromDependencies() {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        classLoaders.add(this);
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
        if (null != clazz) {
            return clazz;
        }
        for(ClassLoader dependency : this.dependsOn) {
            if (dependency instanceof HierarchicalClassLoader) {
                clazz = ((HierarchicalClassLoader) dependency).searchClassInDependencies(className);
                if (null != clazz) {
                    return clazz;
                }
            }
        }
        return null;
    }

    private Class<?> loadClassFromDependencies(String className, ClassLoader scl, boolean[] sclUsed )
            throws ClassNotFoundException {

        for(ClassLoader dependency : dependsOn) {
            try {
                if (dependency instanceof HierarchicalClassLoader) {
                    return ((HierarchicalClassLoader) dependency).loadClassFromDependencies(className, scl, sclUsed);
                } else if (dependency != scl){
                    return dependency.loadClass(className);
                } else if (!sclUsed[0]) {
                    sclUsed[0] = true;
                    return dependency.loadClass(className);
                }
            } catch (ClassNotFoundException e) { }
        }

        return this.findClass(className);
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {

            Class clazz = this.searchClassInDependencies(className);
            if (clazz == null) {
                boolean[] sclUsed = {false};                
                clazz = this.loadClassFromDependencies(className, getSystemClassLoader(), sclUsed);
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }

}
