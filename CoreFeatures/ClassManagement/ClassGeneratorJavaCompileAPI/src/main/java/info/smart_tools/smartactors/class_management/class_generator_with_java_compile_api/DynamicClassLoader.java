package info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extension of {@link URLClassLoader}
 */
public class DynamicClassLoader extends ExtendedURLClassLoader {

    private static ThreadLocal<Map<String,ClassLoader>> currentMap = new ThreadLocal<Map<String,ClassLoader>>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public DynamicClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param uuid the UUID to associate the class loader to
     */
    public DynamicClassLoader(final URL[] urls, UUID uuid) { super(urls, uuid); }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public DynamicClassLoader(final URL[] urls, final ClassLoader parent) { super(urls, parent); }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @param uuid the UUID to associate the class loader to
     */
    public DynamicClassLoader(final URL[] urls, final ClassLoader parent, UUID uuid) {
        super(urls, parent, uuid);
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {

            Class clazz = this.findLoadedClass(className);
            if (null == clazz) {

                Map<String, ClassLoader> cdm = currentMap.get();
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

    public void setClassToClassLoaderMap(Map<String,ClassLoader> classMap) { this.currentMap.set(classMap); }

}
