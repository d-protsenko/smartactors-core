package info.smart_tools.smartactors.class_management.hierarchical_class_loader;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extension of {@link URLClassLoader}
 */
public class SmartactorsClassLoader extends URLClassLoader implements ISmartactorsClassLoader {

    private static Object defaultModuleId = null;
    /* This is ModuleId To ClassLoader Map */
    private static Map<Object, SmartactorsClassLoader> moduleClassLoaders = new ConcurrentHashMap<>();

    private Set<SmartactorsClassLoader> dependsOn = new HashSet<>(); //Collections.synchronizedSet(new HashSet<>());

    public static void setDefaultModuleId(Object moduleId) {
        defaultModuleId = moduleId;
    }

    public static void addModule(Object moduleID, String moduleName, String moduleVersion) {
        SmartactorsClassLoader classLoader = new SmartactorsClassLoader(new URL[]{});
        moduleClassLoaders.put(moduleID, classLoader);
    }

    public static SmartactorsClassLoader getModuleClassLoader(Object moduleID) {
        return moduleClassLoaders.get(moduleID);
    }

    public static void addModuleDependency(Object dependentModuleId, Object baseModuleId) {
        if (!baseModuleId.equals(dependentModuleId)) {
            SmartactorsClassLoader baseClassLoader = getModuleClassLoader(baseModuleId);
            SmartactorsClassLoader dependentClassLoader = getModuleClassLoader(dependentModuleId);
            if (baseClassLoader != null && dependentClassLoader != null) {
                synchronized (dependentClassLoader) {
                    dependentClassLoader.dependsOn.add(baseClassLoader);
                }
            }
        }
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    private SmartactorsClassLoader(final URL[] urls) {
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
            for(SmartactorsClassLoader classLoader : dependsOn) {
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
     * (if dependency is instance of SmartactorsClassLoader)
     * @param className The name of the class to get
     * @return The reference to the class
     */
    private final Class<?> searchClassInDependencies(String className) {
        Class clazz = this.findLoadedClass(className);
        if (null == clazz) {
            for(SmartactorsClassLoader dependency : this.dependsOn) {
                clazz = dependency.searchClassInDependencies(className);
                if (clazz != null) {
                    break;
                }
            }
        }
        return clazz;
    }

    private Class<?> loadClassFromDependencies(String className, ClassLoader scl, ClassLoader dcl, boolean[] rclUsed )
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
            for (SmartactorsClassLoader dependency : dependsOn) {
                try {
                    if (dependency != dcl) {
                        return dependency.loadClassFromDependencies(className, scl, dcl, rclUsed);
                    } else if (!rclUsed[1]) {
                        rclUsed[1] = true;
                        return dependency.loadClassFromDependencies(className, scl, dcl, rclUsed);
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
                        defaultModuleId == null ? null : moduleClassLoaders.get(defaultModuleId),
                        sclUsed
                );
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
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

    public ClassLoader getCompilationClassLoader() { return this; }

    /**
     * Add new instance of {@link URL} to the current url class loader if url class loader doesn't contain this instance of {@link URL}
     * @param url instance of {@link URL}
     */
    public void addURL(final URL url) {
        super.addURL(url);
    }
}
