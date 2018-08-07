package info.smart_tools.smartactors.feature_loading_system.plugin_loader_from_jar;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import sun.misc.PerfCounter;
import sun.misc.Resource;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.Manifest;

/**
 * Extension of {@link URLClassLoader}
 */
public class ExpansibleURLClassLoader extends URLClassLoader {

    private ArrayList<ClassLoader> dependsOn = new ArrayList<ClassLoader>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public ExpansibleURLClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * New constructor
     * @param urls the URLs from which to load classes and resources
     * @param dependsOn the list of class loaders which this class loader depends on
     */
    public ExpansibleURLClassLoader(final URL[] urls, final ArrayList<ClassLoader> dependsOn) {
        super(urls);
        this.dependsOn = dependsOn;
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public ExpansibleURLClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * New constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @param dependsOn the list of class loaders which this class loader depends on
     */
    public ExpansibleURLClassLoader(final URL[] urls, final ClassLoader parent, final ArrayList<ClassLoader> dependsOn) {
        super(urls, parent);
        this.dependsOn = dependsOn;
    }

    /**
     * Add new instance of {@link URL} to the current url class loader if url class loader doesn't contain this instance of {@link URL}
     * @param url instance of {@link URL}
     */
    public void addUrl(final URL url) {
        URL[] urls = getURLs();
        if (Arrays.asList(urls).contains(url)) {
            return;
        }
        addURL(url);
    }

    /**
     * Add new dependency on {@link ClassLoader} to this {@link ExpansibleURLClassLoader}
     * @param classLoader {@link ClassLoader} which this {@link ExpansibleURLClassLoader} depends on
     */
    public void addDependency(ClassLoader classLoader)
            throws InvalidArgumentException {
        if (null == classLoader) {
            throw new InvalidArgumentException("Class loader can't have null dependency.");
        }
        dependsOn.add(classLoader);
    }

    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {
            Class clazz = this.findLoadedClass(className);

            if (clazz == null) {
                for(ClassLoader classLoader : dependsOn) {
                    try {
                        clazz = classLoader.loadClass(className);
                        break;
                    } catch (ClassNotFoundException e) { }
                }

                if (clazz == null) {
                    clazz = super.loadClass(className, false);
                }
            }

            if (resolve) {
                this.resolveClass(clazz);
            }

            return clazz;
        }
    }

}
