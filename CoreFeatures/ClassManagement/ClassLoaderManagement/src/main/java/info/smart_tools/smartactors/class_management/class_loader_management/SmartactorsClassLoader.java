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
    private static Map<String, SmartactorsClassLoader> itemClassLoaders =
            new ConcurrentHashMap<String, SmartactorsClassLoader>();

    /* This is ItemID To (ClassName To ClassLoader Map) Map */
    private static Map<String, Map<String, ClassLoader>> itemClassMap =
            new ConcurrentHashMap<String, Map<String, ClassLoader>>();

    /* This is ClassName To ClassLoader Map in current Thread */
    private static ThreadLocal<Map<String, ClassLoader>> currentClassMap =
            new ThreadLocal<Map<String, ClassLoader>>();

    private String itemID;
    private String itemName = null;
    private List<ClassLoader> dependsOn = new ArrayList<ClassLoader>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private SmartactorsClassLoader(final URL[] urls, String itemID) {
        super(urls);
        this.itemID = itemID;
    }

    static void addItem(String itemID) {
        SmartactorsClassLoader classLoader = new SmartactorsClassLoader(new URL[]{}, itemID);
        itemClassLoaders.put(itemID, classLoader);

        Map<String, ClassLoader> classMap = new ConcurrentHashMap<String, ClassLoader>();
        itemClassMap.put(itemID, classMap);
    }

    static void setItemName(String itemID, String itemName) {
        //Map<String,ClassLoader> classMap = itemClassMap.get(itemID);
        SmartactorsClassLoader classLoader = itemClassLoaders.get(itemID);
        classLoader.itemName = itemName;
        //classMap.put(itemName, classLoader);
    }

    static void addItemDependency(String itemIdDependent, String itemIdOnWhichDepend) {
        SmartactorsClassLoader classLoader = itemClassLoaders.get(itemIdOnWhichDepend);
        itemClassLoaders.get(itemIdDependent).addDependency(classLoader);
        Map<String,ClassLoader> dependentMap = itemClassMap.get(itemIdDependent);
        dependentMap.put(classLoader.itemName, classLoader); //
        //Map<String,ClassLoader> map = itemClassMap.get(itemIdOnWhichDepend);
        //dependentMap.putAll(map);
    }

    static void finalizeItemDependencies(String itemID, String defaultItemID) {
        if (itemClassLoaders.get(itemID).getDependencies().size() == 0) {
            addItemDependency(itemID, defaultItemID);
        }
    }

    static ISmartactorsClassLoader getItemClassLoader(String itemID) {
        return itemClassLoaders.get(itemID);
    }

    /*static void setCurrentItem(String itemID) {
        Map<String,ClassLoader> classMap = itemClassMap.get(itemID);
        currentClassMap.set(classMap);
    }*/

    private void addDependency(SmartactorsClassLoader classLoader) {
        if (null != classLoader) {
            dependsOn.add(classLoader);
        }
    }

    private List<ClassLoader> getDependencies() {
        return this.dependsOn;
    }

    public void addUrl(final URL url) {
        URL[] urls = this.getURLs();
        if (!Arrays.asList(urls).contains(url)) {
            this.addURL(url);
        }
    }

    /* here we collect all class loaders from dependency map recursively */
    private void addDependenciesToSet(Set<ClassLoader> classLoaders) {

        classLoaders.add(this);
        if (dependsOn.size() == 0) {
            ClassLoader cl = this.getParent();
            while(cl != null) {
                if (cl instanceof URLClassLoader) {
                    classLoaders.add(cl);
                }
                cl = cl.getParent();
            }
        } else {
            for (ClassLoader dependency : this.dependsOn) {
                if (dependency instanceof SmartactorsClassLoader) {
                    ((SmartactorsClassLoader) dependency).addDependenciesToSet(classLoaders);
                } else if (dependency instanceof URLClassLoader) {
                    classLoaders.add((ClassLoader)dependency);
                }
            }
        }
    }

    public URL[] getURLsFromDependencies() {

        Set<ClassLoader> classLoaders = new HashSet<ClassLoader>();
        addDependenciesToSet(classLoaders);

        ArrayList<URL> urlArrayList = new ArrayList<>();
        for( ClassLoader classLoader : classLoaders) {
            Collections.addAll(urlArrayList,((URLClassLoader)classLoader).getURLs());
        }

        URL[] urls = new URL[urlArrayList.size()];
        urlArrayList.toArray(urls);

        return urls;
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {

            Class clazz = this.findLoadedClass(className);
            if (null == clazz) {

                Map<String, ClassLoader> cdm = itemClassMap.get(this.itemID); // ??? may be this is correct
                //Map<String, ClassLoader> cdm = currentClassMap.get();       // HBZ
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

    public Class<?> addClass(final String className, byte[] classByteCode) {
        return defineClass(className, classByteCode, 0, classByteCode.length);
    }
}
