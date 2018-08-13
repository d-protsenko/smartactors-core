package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Tests for ExtendedURLClassLoader
 */
public class ExtendedURLClassLoaderTest {

    @Test
    public void checkExpansibleURLClassLoaderCreation()
            throws Exception {
        ExtendedURLClassLoader cl1 = new ExtendedURLClassLoader(new URL[]{});
        assertNotNull(cl1);
        ExtendedURLClassLoader cl2 = new ExtendedURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
        assertNotNull(cl2);
        assertSame(cl2.getParent(), ClassLoader.getSystemClassLoader());
    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
        URL url1 = new URL("http", "host", 9000, "filename1");
        URL url2 = new URL("http", "host", 9000, "filename2");
        URL url3 = new URL("http", "host", 9000, "filename1");
        ExtendedURLClassLoader c0 = new ExtendedURLClassLoader(new URL[]{});
        c0.addDependency(this.getClass().getClassLoader());
        ExtendedURLClassLoader cl = new ExtendedURLClassLoader(new URL[]{url1}, c0);
        assertNotNull(cl);
        cl.addUrl(url2);
        cl.addUrl(url3);
        URL[] result = cl.getURLs();
        assertSame(result[0], url1);
        assertSame(result[1], url2);
        assertEquals(result.length, 2);
        assertEquals(cl.getDependencies().size(), 1);

        try {
            cl.addDependency(null);
            fail();
        } catch (InvalidArgumentException e) {
        }

        try {
            Class clazz = cl.loadClass("nowhere.classNotExist");
            fail();
        } catch (ClassNotFoundException e) {
        }

        Class clazz = cl.loadClass("java.lang.String", true);
    }

    @Test
    public void checkGettersSetters()
            throws Exception {

        ExtendedURLClassLoader cl = new ExtendedURLClassLoader(
                new URL[]{}, this.getClass().getClassLoader()
        );
        assertEquals(cl.getDependencies().size(), 1);

        String namespace = "currentClassLoaderNamespace";
        cl.setNamespace(namespace);
        assertEquals(cl.getNamespace(), namespace);

        Class clazz = cl.getLoadedClass("unregistered");
        assertEquals(clazz, null);
        byte[] byteCode = null;

        try {
            clazz = cl.addClass("newClass", byteCode);
            fail();
        } catch (NullPointerException e) { }

    }
}