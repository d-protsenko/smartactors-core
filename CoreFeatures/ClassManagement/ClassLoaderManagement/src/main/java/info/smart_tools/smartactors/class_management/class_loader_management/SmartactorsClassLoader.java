package info.smart_tools.smartactors.class_management.class_loader_management;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link URLClassLoader}
 */
public class SmartactorsClassLoader extends URLClassLoader implements ISmartactorsClassLoader {

    /* This is ItemID To ClassLoader Map */
    private static Map<String, ISmartactorsClassLoader> itemClassLoaders =
            new ConcurrentHashMap<String, ISmartactorsClassLoader>();

    /* This is ItemID To (ClassName To ClassLoader Map) Map */
    private static Map<String,Map<String,ClassLoader>> itemClassMap =
            new ConcurrentHashMap<String,Map<String,ClassLoader>>();

    /* This is ClassName To ClassLoader Map in current Thread */
    private static ThreadLocal<Map<String,ClassLoader>> currentClassMap =
            new ThreadLocal<Map<String,ClassLoader>>();

    private ArrayList<ISmartactorsClassLoader> dependsOn = new ArrayList<ISmartactorsClassLoader>();
    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    SmartactorsClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public SmartactorsClassLoader(final URL[] urls, final ClassLoader parent) { super(urls, parent); }

    static void addItem(String itemID, String itemName) {
        SmartactorsClassLoader classLoader = new SmartactorsClassLoader(new URL[]{});
        Map<String,ClassLoader> classMap = new ConcurrentHashMap<String,ClassLoader>();
        classMap.put(itemName, classLoader);
        itemClassMap.put(itemID, classMap);
    }

    static void addItemDependency(String itemIdDependent, String itemIdOnWhichDepend) {
        Map<String,ClassLoader> dependentMap = itemClassMap.get(itemIdDependent);
        Map<String,ClassLoader> map = itemClassMap.get(itemIdOnWhichDepend);
        dependentMap.putAll(map);

        itemClassLoaders.get(itemIdDependent).addDependency(itemClassLoaders.get(itemIdOnWhichDepend));
    }

    static void finalizeItemDependencies(String itemID, String defaultItemID) {
        if (itemClassLoaders.get(itemID).getDependencies().size() == 0) {
            addItemDependency(itemID, defaultItemID);
        }
    }

    static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return itemClassLoaders.get(itemID);
    }

    static void setCurrentItem(String itemID) {
        Map<String,ClassLoader> classMap = itemClassMap.get(itemID);
        currentClassMap.set(classMap);
    }

    public void addDependency(ISmartactorsClassLoader classLoader) {
        if (null != classLoader) {
            dependsOn.add(classLoader);
        }
    }

    public List<ISmartactorsClassLoader> getDependencies() {
        return this.dependsOn;
    }

    public void addUrl(final URL url) {
        URL[] urls = this.getURLs();
        if (!Arrays.asList(urls).contains(url)) {
            this.addURL(url);
        }
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {

            Class clazz = this.findLoadedClass(className);
            if (null == clazz) {

                Map<String, ClassLoader> cdm = currentClassMap.get();
                if (cdm != null) {

                    ClassLoader classLoader = cdm.get(className);
                    if (classLoader == null) {
                        if (this.getParent() != null ) {
                            try {
                                clazz = this.getParent().loadClass(className);
                            } catch (ClassNotFoundException e) {}
                        }
                    }

                    if (clazz == null) {
                        String name = className;
                        int index;
                        while(classLoader == null) {
                            index = name.lastIndexOf(".");
                            if (index == -1) {
                                break;
                            }
                            name = name.substring(0, index);
                            classLoader = cdm.get(name);
                        }
                        if (classLoader != null) {
                            if (this != classLoader) {
                                try {
                                    clazz = classLoader.loadClass(className);
                                    // this for case if findClass done without cdm
                                    if (cdm.get(className) == null) {
                                        cdm.put(className, classLoader);
                                    }
                                } catch (ClassNotFoundException e) {}
                            }
                        }
                    }
                } else {
                    if (this.getParent() != null ) {
                        try {
                            clazz = this.getParent().loadClass(className);
                        } catch (ClassNotFoundException e) {}
                    }
                }


                if (clazz == null) {
                    clazz = this.findClass(className);

                    if (clazz != null && cdm != null) {
                        cdm.put(className,this);
                    }
                }
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }
}
