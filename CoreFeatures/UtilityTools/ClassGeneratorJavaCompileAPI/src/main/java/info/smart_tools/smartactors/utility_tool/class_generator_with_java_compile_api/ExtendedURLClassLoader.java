package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extension of {@link URLClassLoader}
 */
public class ExtendedURLClassLoader extends URLClassLoader {

    private String namespace = "";
    private ArrayList<ClassLoader> dependsOn = new ArrayList<ClassLoader>();
    //private Map<String, CompiledCode> compiledCodeStorage = new HashMap<>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public ExtendedURLClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public ExtendedURLClassLoader(final URL[] urls, final ClassLoader parent)
            throws InvalidArgumentException {
        super(urls, parent);
        this.addDependency(parent);
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
     * Add new dependency on {@link ClassLoader} to this {@link ExtendedURLClassLoader}
     * @param classLoader {@link ClassLoader} which this {@link ExtendedURLClassLoader} depends on
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
                    clazz = this.findClass(className);
                }
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

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
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

    /**
     * Get the class from this class loader only (not from parents or dependencies)
     * @param className The name of the class to get
     * @return The reference to the class from this class loader
     */
    public final Class<?> getLoadedClass(String className) {
        return this.findLoadedClass(className);
    }
}
