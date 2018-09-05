package info.smart_tools.smartactors.class_management.class_loader_management;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link URLClassLoader}
 */
public class SmartactorsClassLoader extends ExtendedURLClassLoader implements ISmartactorsClassLoader {

    /* This is ItemID To ClassLoader Map */
    private static Map<String, SmartactorsClassLoader> itemClassLoaders =
            new ConcurrentHashMap<String, SmartactorsClassLoader>();

    private String itemName = null;
    private Set<SmartactorsClassLoader> dependsOn = new HashSet<SmartactorsClassLoader>();
    private Map<String, ClassLoader> classMap = new ConcurrentHashMap<String, ClassLoader>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private SmartactorsClassLoader(final URL[] urls) {
        super(urls);
    }

    static void addItem(String itemID) {
        SmartactorsClassLoader classLoader = new SmartactorsClassLoader(new URL[]{});
        itemClassLoaders.put(itemID, classLoader);
    }

    static SmartactorsClassLoader getItemClassLoader(String itemID) {
        return itemClassLoaders.get(itemID);
    }

    static void setItemName(String itemID, String itemName) {
        itemName = itemName.replace('/', '.');
        itemName = itemName.replace(':', '.');
        itemName = itemName.replace('-', '_');
        SmartactorsClassLoader classLoader = getItemClassLoader(itemID);
        classLoader.itemName = itemName;
        classLoader.classMap.put(itemName, classLoader);
    }

    static void addItemDependency(String dependentItemID, String baseItemID) {
        if (!baseItemID.equals(dependentItemID)) {
            SmartactorsClassLoader baseClassLoader = getItemClassLoader(baseItemID);
            SmartactorsClassLoader dependentClassLoader = getItemClassLoader(dependentItemID);
            if (baseClassLoader != null && dependentClassLoader != null) {
                Set<SmartactorsClassLoader> classLoaders = new HashSet<SmartactorsClassLoader>();
                classLoaders.add(baseClassLoader);
                classLoaders.addAll(baseClassLoader.dependsOn);
                for (ClassLoader cl : classLoaders) {
                    dependentClassLoader.classMap.put(((SmartactorsClassLoader) cl).itemName, cl);
                }
                dependentClassLoader.dependsOn.addAll(classLoaders);
            }
        }
    }

    static void finalizeItemDependencies(String itemID, String defaultItemID) {
        if (getItemClassLoader(itemID).dependsOn.size() == 0) {
            addItemDependency(itemID, defaultItemID);
        }
    }

    public URL[] getURLsFromDependencies() {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        classLoaders.add(this);
        classLoaders.addAll(dependsOn);
        for(ClassLoader classLoader : dependsOn) {
            ClassLoader parent = classLoader.getParent();
            while(parent != null) {
                classLoaders.add(parent);
                parent = parent.getParent();
            }
        }

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

    private Class<?> loadClass0(String className, boolean upperLevel)
            throws ClassNotFoundException {

        Class clazz = this.findLoadedClass(className);
        if (clazz == null) {

            ClassLoader classLoader = classMap.get(className);
            if (classLoader != null) {
                try {
                    clazz = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) { }
            }

            if (clazz == null) {
                if (this.getParent() != null) {
                    try {
                        clazz = this.getParent().loadClass(className);
                    } catch (ClassNotFoundException e) { }
                }

                if (clazz == null && upperLevel) {
                    String name = className;
                    do {
                        int index = name.lastIndexOf(".");
                        if (index == -1) {
                            break;
                        }
                        name = name.substring(0, index);
                        classLoader = classMap.get(name);
                    } while (classLoader == null);

                    if (classLoader != null && this != classLoader) {
                        try {
                            clazz = classLoader.loadClass(className);
                            classMap.put(className, clazz.getClassLoader());
                        } catch (ClassNotFoundException e) { }
                    }
                }

                if (clazz == null && upperLevel) {
                    for (SmartactorsClassLoader dependency : dependsOn) {
                        if (dependency != classLoader) {
                            try {
                                clazz = dependency.loadClass0(className, false);
                                classMap.put(className, clazz.getClassLoader());
                                break;
                            } catch (ClassNotFoundException e) { }
                        }
                    }
                }

                if (clazz == null) {
                    clazz = this.findClass(className);
                }
            }
        }

        return clazz;
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized (this.getClassLoadingLock(className)) {

            Class clazz = loadClass0(className, true);
            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }

    public ClassLoader getCompilationClassLoader() { return this; }
}
