package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extension of {@link URLClassLoader}
 */
public class ExpansibleURLClassLoader extends URLClassLoader {

    private String namespace = "";
    private ArrayList<ClassLoader> dependsOn = new ArrayList<ClassLoader>();
    //private Map<String, CompiledCode> compiledCodeStorage = new HashMap<>();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public ExpansibleURLClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public ExpansibleURLClassLoader(final URL[] urls, final ClassLoader parent)
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

    /**
     * Add instance of {@link CompiledCode} to the local storage
     * @param cc instance of {@link CompiledCode}
     *
    void setCode(final CompiledCode cc) {
        compiledCodeStorage.put(cc.getName(), cc);
    }

    @Override
    protected Class<?> findClass(final String name)
            throws ClassNotFoundException {
        CompiledCode cc = compiledCodeStorage.get(name);
        if (cc == null) {
            return super.findClass(name);
        }
        byte[] byteCode = cc.getByteCode();
        return defineClass(name, byteCode, 0, byteCode.length);
    }
*/
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Class<?> addClass(final String className, byte[] classByteCode) {
        return defineClass(className, classByteCode, 0, classByteCode.length);
    }
}
