package info.smart_tools.smartactors.class_management.class_loader_management;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Extension of {@link URLClassLoader}
 */
public class ExtendedURLClassLoader extends URLClassLoader {

    private String namespace = "";
    private UUID classLoaderUUID = java.util.UUID.randomUUID();

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     */
    public ExtendedURLClassLoader(final URL[] urls) { super(urls); }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param uuid the UUID to associate the class loader with
     */
    public ExtendedURLClassLoader(final URL[] urls, UUID uuid) {
        super(urls);
        classLoaderUUID = uuid;
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public ExtendedURLClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Redefined constructor
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public ExtendedURLClassLoader(final URL[] urls, final ClassLoader parent, UUID uuid) {
        super(urls, parent);
        classLoaderUUID = uuid;
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

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public UUID getClassLoaderUUID() {
        return this.classLoaderUUID;
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

}
