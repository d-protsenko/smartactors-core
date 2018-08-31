package info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Extension of {@link URLClassLoader}
 */
public class HierarchicalClassLoader extends ExtendedURLClassLoader {

    private ArrayList<ClassLoader> dependsOn = new ArrayList<ClassLoader>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public HierarchicalClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param uuid the UUID to associate the class loader to
     */
    public HierarchicalClassLoader(final URL[] urls, UUID uuid) { super(urls, uuid); }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public HierarchicalClassLoader(final URL[] urls, final ClassLoader parent)
            throws InvalidArgumentException {
        super(urls, parent);
        this.addDependency(parent);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @param uuid the UUID to associate the class loader to
     */
    public HierarchicalClassLoader(final URL[] urls, final ClassLoader parent, UUID uuid) {
        super(urls, parent, uuid);
    }

    /**
     * Add new dependency on {@link ClassLoader} to this {@link HierarchicalClassLoader}
     * @param classLoader {@link ClassLoader} which this {@link HierarchicalClassLoader} depends on
     */
    public void addDependency(ClassLoader classLoader)
            throws InvalidArgumentException {
        if (null == classLoader) {
            throw new InvalidArgumentException("Class loader can't have null dependency.");
        }
        dependsOn.add(classLoader);
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

    /**
     * @return list of class loaders which this class loader depends on
     */
    public final List<ClassLoader> getDependencies() {
        return this.dependsOn;
    }

}
