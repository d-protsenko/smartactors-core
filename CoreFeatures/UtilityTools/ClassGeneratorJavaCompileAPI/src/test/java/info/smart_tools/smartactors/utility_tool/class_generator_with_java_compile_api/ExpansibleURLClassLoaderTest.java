package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.ExpansibleURLClassLoader;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Tests for ExpansibleURLClassLoader
 */
public class ExpansibleURLClassLoaderTest {

    @Test
    public void checkExpansibleURLClassLoaderCreation()
            throws Exception {
        ExpansibleURLClassLoader cl1 = new ExpansibleURLClassLoader(new URL[]{});
        assertNotNull(cl1);
        ExpansibleURLClassLoader cl2 = new ExpansibleURLClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
        assertNotNull(cl2);
        assertSame(cl2.getParent(), ClassLoader.getSystemClassLoader());
    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
        URL url1 = new URL("http", "host", 9000, "filename1");
        URL url2 =  new URL("http", "host", 9000, "filename2");
        URL url3 =  new URL("http", "host", 9000, "filename1");
        ExpansibleURLClassLoader c0 = new ExpansibleURLClassLoader(new URL[]{});
        c0.addDependency(this.getClass().getClassLoader());
        ExpansibleURLClassLoader cl = new ExpansibleURLClassLoader(new URL[]{url1}, c0);
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
        } catch (InvalidArgumentException e) {}

        try {
            Class clazz = cl.loadClass("nowhere.classNotExist");
            fail();
        } catch (ClassNotFoundException e) {}

        Class clazz = cl.loadClass("java.lang.String", true);
    }
}
