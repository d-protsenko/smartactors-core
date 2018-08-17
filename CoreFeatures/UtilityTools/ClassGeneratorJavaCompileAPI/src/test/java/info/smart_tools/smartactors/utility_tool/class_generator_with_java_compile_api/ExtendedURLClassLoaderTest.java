package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

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

        try {
            byte[] byteCode = { 1, 2 };
            Class clazz = cl.addClass("newClass", byteCode);
            fail();
        } catch (ClassFormatError e) { }
    }

    private void attachJarToClassLoader(String jarName, ExtendedURLClassLoader cl)
            throws Exception {
        String pathToJar = this.getClass().getClassLoader().getResource(jarName).getFile();
        cl.addUrl(new URL("jar:file:" + pathToJar+"!/"));
    }

    private URLClassLoader createClassLoaderForJar(String jarName, ClassLoader parent)
            throws Exception {
        URLClassLoader cl;
        String pathToJar = this.getClass().getClassLoader().getResource(jarName).getFile();
        URL url = new URL("jar:file:" + pathToJar+"!/");
        URL[] urLs = {url};
        cl = new URLClassLoader(urLs, parent);
        return cl;
    }

    @Test
    public void performanceTestForExtendedClassLoader()
            throws Exception {
        {
            ExtendedURLClassLoader cl00 = new ExtendedURLClassLoader(new URL[]{});
            cl00.setNamespace("DeadEnd-IField");
            ExtendedURLClassLoader cl01 = new ExtendedURLClassLoader(new URL[]{});
            cl01.setNamespace("PathToSCL");
            ExtendedURLClassLoader cl1 = new ExtendedURLClassLoader(new URL[]{});
            cl1.setNamespace("CL1");
            ExtendedURLClassLoader cl2 = new ExtendedURLClassLoader(new URL[]{});
            cl2.setNamespace("CL2");
            ExtendedURLClassLoader cl3 = new ExtendedURLClassLoader(new URL[]{});
            cl3.setNamespace("CL3");
            ExtendedURLClassLoader cl4 = new ExtendedURLClassLoader(new URL[]{});
            cl4.setNamespace("CL4");
            cl01.addDependency(ClassLoader.getSystemClassLoader());
            cl1.addDependency(cl00);
            cl1.addDependency(cl01);
            cl2.addDependency(cl01);
            cl3.addDependency(cl1);
            cl3.addDependency(cl2);
            cl4.addDependency(cl2);
            attachJarToClassLoader("ifield.jar", cl00);
            attachJarToClassLoader("ifield.jar", cl01);
            attachJarToClassLoader("ifield_name.jar", cl2);
            attachJarToClassLoader("iobject.jar", cl2);
            attachJarToClassLoader("iobject-wrapper.jar", cl2);
            try {
                cl3.loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
                fail();
            } catch (NoClassDefFoundError e) { }
        }
        Class clazz = null;
        long iterations, t1, t2, t3, t4;
        double d, a, u;
        d = 0; a = 0; u = 0;
        iterations = 1000;
        for(int i=0; i<iterations; i++)
        {
            ExtendedURLClassLoader cl00 = new ExtendedURLClassLoader(new URL[]{});
            cl00.setNamespace("DeadEnd-IField");
            ExtendedURLClassLoader cl01 = new ExtendedURLClassLoader(new URL[]{});
            cl01.setNamespace("PathToSCL");
            ExtendedURLClassLoader cl1 = new ExtendedURLClassLoader(new URL[]{});
            cl1.setNamespace("CL1");
            ExtendedURLClassLoader cl2 = new ExtendedURLClassLoader(new URL[]{});
            cl2.setNamespace("CL2");
            ExtendedURLClassLoader cl3 = new ExtendedURLClassLoader(new URL[]{});
            cl3.setNamespace("CL3");
            ExtendedURLClassLoader cl4 = new ExtendedURLClassLoader(new URL[]{});
            cl4.setNamespace("CL4");
            cl01.addDependency(ClassLoader.getSystemClassLoader());
            cl1.addDependency(cl00);
            cl1.addDependency(cl01);
            cl2.addDependency(cl01);
            cl3.addDependency(cl1);
            cl3.addDependency(cl2);
            cl4.addDependency(cl2);
            attachJarToClassLoader("ifield.jar", cl00);
            attachJarToClassLoader("ifield.jar", cl01);
            attachJarToClassLoader("ifield_name.jar", cl2);
            attachJarToClassLoader("iobject.jar", cl2);
            attachJarToClassLoader("iobject-wrapper.jar", cl2);
            t1 = System.nanoTime();
            clazz = cl2.loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
            t2 = System.nanoTime();
            clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject.IObject");
            t3 = System.nanoTime();
            d += t2-t1;
            a += t3-t2;
        }
        d /= iterations;
        a /= iterations;
        System.out.println("");
        System.out.println("Average direct line class loading (ns): "+d);
        System.out.println("Average aroung line class loading (ns): "+a);

        ExtendedURLClassLoader cl00 = new ExtendedURLClassLoader(new URL[]{});
        cl00.setNamespace("DeadEnd-IField");
        ExtendedURLClassLoader cl01 = new ExtendedURLClassLoader(new URL[]{});
        cl01.setNamespace("PathToSCL");
        ExtendedURLClassLoader cl1 = new ExtendedURLClassLoader(new URL[]{});
        cl1.setNamespace("CL1");
        ExtendedURLClassLoader cl2 = new ExtendedURLClassLoader(new URL[]{});
        cl2.setNamespace("CL2");
        ExtendedURLClassLoader cl3 = new ExtendedURLClassLoader(new URL[]{});
        cl3.setNamespace("CL3");
        ExtendedURLClassLoader cl4 = new ExtendedURLClassLoader(new URL[]{});
        cl4.setNamespace("CL4");
        ExtendedURLClassLoader cl5 = new ExtendedURLClassLoader(new URL[]{});
        cl5.setNamespace("CL5");
        ExtendedURLClassLoader cl6 = new ExtendedURLClassLoader(new URL[]{});
        cl6.setNamespace("CL6");
        ExtendedURLClassLoader cl7 = new ExtendedURLClassLoader(new URL[]{});
        cl7.setNamespace("CL7");
        cl01.addDependency(ClassLoader.getSystemClassLoader());
        cl1.addDependency(cl00);
        cl1.addDependency(cl01);
        cl2.addDependency(cl01);
        cl3.addDependency(cl1);
        cl3.addDependency(cl2);
        cl4.addDependency(cl2);
        cl5.addDependency(cl4);
        cl6.addDependency(cl5);
        cl7.addDependency(cl6);
        attachJarToClassLoader("ifield.jar", cl00);
        attachJarToClassLoader("ifield.jar", cl01);
        attachJarToClassLoader("ifield_name.jar", cl2);
        attachJarToClassLoader("iobject.jar", cl2);
        attachJarToClassLoader("iobject-wrapper.jar", cl2);
        URLClassLoader cl8 = createClassLoaderForJar("ifield.jar", ClassLoader.getSystemClassLoader());
        URLClassLoader cl9 = createClassLoaderForJar("ifield_name.jar", cl8);
        URLClassLoader cl10 = createClassLoaderForJar("iobject.jar", cl9);
        URLClassLoader cl11 = createClassLoaderForJar("iobject-wrapper.jar", cl10);
        URLClassLoader cl12 = new URLClassLoader(new URL[]{}, cl11);
        URLClassLoader cl13 = new URLClassLoader(new URL[]{}, cl12);
        URLClassLoader cl14 = new URLClassLoader(new URL[]{}, cl13);
        URLClassLoader cl15 = new URLClassLoader(new URL[]{}, cl14);
        URLClassLoader cl16 = new URLClassLoader(new URL[]{}, cl15);
        clazz = cl2.loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
        clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject.IObject");
        clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        clazz = cl7.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        clazz = cl15.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        d = 0; a = 0; u = 0;
        iterations = 100000;
        for(int i=0; i<iterations; i++)
        {
            t1 = System.nanoTime();
            clazz = cl7.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t2 = System.nanoTime();
            clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t3 = System.nanoTime();
            clazz = cl15.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t4 = System.nanoTime();
            d += t2-t1;
            a += t3-t2;
            u += t4-t3;
        }
        d /= iterations;
        a /= iterations;
        u /= iterations;
        System.out.println("Average direct line class reading (ns): "+d);
        System.out.println("Average around line class reading (ns): "+a);
        System.out.println("Average descendant line class reading (ns): "+u);
    }
}