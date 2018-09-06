package info.smart_tools.smartactors.class_management.class_loader_management;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * Tests for SmartactorsClassLoader
 */
public class SmartactorsClassLoaderTest {

    @Test
    public void checkSmartactorsClassLoaderCreation()
            throws Exception {
        VersionControlProvider.addItem(VersionControlProvider.coreID);
        VersionControlProvider.addItem("cl1");
        assertNotNull(VersionControlProvider.getItemClassLoader("cl1"));
        VersionControlProvider.addItem("cl2");
        assertNotNull(VersionControlProvider.getItemClassLoader("cl2"));
        assertSame(((ClassLoader) VersionControlProvider.getItemClassLoader("cl2")).getParent(), ClassLoader.getSystemClassLoader());
        VersionControlProvider.addItemDependency("cl1", "cl2");
        VersionControlProvider.finalizeItemDependencies("cl1", VersionControlProvider.coreID);
        VersionControlProvider.finalizeItemDependencies("cl2", VersionControlProvider.coreID);
    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
        URL url1 = new URL("http", "host", 9000, "filename1");
        URL url2 = new URL("http", "host", 9000, "filename2");
        URL url3 = new URL("http", "host", 9000, "filename1");
        VersionControlProvider.addItem("c0");
        VersionControlProvider.addItem("cl");
        ISmartactorsClassLoader cl = VersionControlProvider.getItemClassLoader("cl");

        assertNotNull(cl);
        cl.addURL(url2);
        cl.addURL(url3);
        URL[] result = cl.getURLsFromDependencies();
        assertSame(result[0], url1);
        assertSame(result[1], url2);
        assertEquals(result.length, 2);

        try {
            Class clazz = cl.loadClass("nowhere.classNotExist");
            fail();
        } catch (ClassNotFoundException e) {
        }

        Class clazz = cl.loadClass("java.lang.String");
    }

    @Test
    public void checkGettersSetters()
            throws Exception {

        VersionControlProvider.addItem("cl");
        ISmartactorsClassLoader cl = VersionControlProvider.getItemClassLoader("cl");

        String namespace = "currentClassLoaderNamespace";
        VersionControlProvider.setItemName("cl", namespace);

        try {
            byte[] byteCode = { 1, 2 };
            Class clazz = cl.addClass("newClass", byteCode);
            fail();
        } catch (ClassFormatError e) { }
    }

    private void attachJarToClassLoader(String jarName, ISmartactorsClassLoader cl)
            throws Exception {
        String pathToJar = this.getClass().getClassLoader().getResource(jarName).getFile();
        cl.addURL(new URL("jar:file:" + pathToJar+"!/"));
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
    @Ignore
    public void performanceTestForExtendedClassLoader()
            throws Exception {
        {
            VersionControlProvider.addItem("cl00");
            VersionControlProvider.setItemName("cl00", "DeadEnd-IField");
            ISmartactorsClassLoader cl00 = VersionControlProvider.getItemClassLoader("cl00");
            VersionControlProvider.addItem("cl01");
            VersionControlProvider.setItemName("cl01", "PathToSCL-IField");
            ISmartactorsClassLoader cl01 = VersionControlProvider.getItemClassLoader("cl01");
            VersionControlProvider.addItem("CL1");
            VersionControlProvider.setItemName("CL1", "CL1");
            ISmartactorsClassLoader cl1 = VersionControlProvider.getItemClassLoader("CL1");
            VersionControlProvider.addItem("CL2");
            VersionControlProvider.setItemName("CL2", "CL2");
            ISmartactorsClassLoader cl2 = VersionControlProvider.getItemClassLoader("CL2");
            VersionControlProvider.addItem("CL3");
            VersionControlProvider.setItemName("CL3", "CL3");
            ISmartactorsClassLoader cl3 = VersionControlProvider.getItemClassLoader("CL3");
            VersionControlProvider.addItem("CL4");
            VersionControlProvider.setItemName("CL4", "CL4");
            ISmartactorsClassLoader cl4 = VersionControlProvider.getItemClassLoader("CL4");
            VersionControlProvider.addItemDependency("CL1", "cl00");
            VersionControlProvider.addItemDependency("CL1", "cl01");
            VersionControlProvider.addItemDependency("CL2", "cl01");
            VersionControlProvider.addItemDependency("CL3", "CL1");
            VersionControlProvider.addItemDependency("CL3", "CL2");
            VersionControlProvider.addItemDependency("CL4", "CL2");
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
        for(int i=0; i<iterations+5; i++)
        {
            VersionControlProvider.addItem("cl00");
            VersionControlProvider.setItemName("cl00", "DeadEnd-IField");
            ISmartactorsClassLoader cl00 = VersionControlProvider.getItemClassLoader("cl00");
            VersionControlProvider.addItem("cl01");
            VersionControlProvider.setItemName("cl01", "PathToSCL-IField");
            ISmartactorsClassLoader cl01 = VersionControlProvider.getItemClassLoader("cl01");
            VersionControlProvider.addItem("CL1");
            VersionControlProvider.setItemName("CL1", "CL1");
            ISmartactorsClassLoader cl1 = VersionControlProvider.getItemClassLoader("CL1");
            VersionControlProvider.addItem("CL2");
            VersionControlProvider.setItemName("CL2", "CL2");
            ISmartactorsClassLoader cl2 = VersionControlProvider.getItemClassLoader("CL2");
            VersionControlProvider.addItem("CL3");
            VersionControlProvider.setItemName("CL3", "CL3");
            ISmartactorsClassLoader cl3 = VersionControlProvider.getItemClassLoader("CL3");
            VersionControlProvider.addItem("CL4");
            VersionControlProvider.setItemName("CL4", "CL4");
            ISmartactorsClassLoader cl4 = VersionControlProvider.getItemClassLoader("CL4");
            VersionControlProvider.addItemDependency("CL1", "cl00");
            VersionControlProvider.addItemDependency("CL1", "cl01");
            VersionControlProvider.addItemDependency("CL2", "cl01");
            VersionControlProvider.addItemDependency("CL3", "CL1");
            VersionControlProvider.addItemDependency("CL3", "CL2");
            VersionControlProvider.addItemDependency("CL4", "CL2");

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
            if (i>4) {
                d += t2 - t1;
                a += t3 - t2;
            }
        }
        d /= iterations;
        a /= iterations;
        System.out.println("");
        System.out.println("Average direct line class loading (ns): "+d);
        System.out.println("Average aroung line class loading (ns): "+a);

        VersionControlProvider.addItem("cl00");
        VersionControlProvider.setItemName("cl00", "DeadEnd-IField");
        ISmartactorsClassLoader cl00 = VersionControlProvider.getItemClassLoader("cl00");
        VersionControlProvider.addItem("cl01");
        VersionControlProvider.setItemName("cl01", "PathToSCL-IField");
        ISmartactorsClassLoader cl01 = VersionControlProvider.getItemClassLoader("cl01");
        VersionControlProvider.addItem("CL1");
        VersionControlProvider.setItemName("CL1", "CL1");
        ISmartactorsClassLoader cl1 = VersionControlProvider.getItemClassLoader("CL1");
        VersionControlProvider.addItem("CL2");
        VersionControlProvider.setItemName("CL2", "CL2");
        ISmartactorsClassLoader cl2 = VersionControlProvider.getItemClassLoader("CL2");
        VersionControlProvider.addItem("CL3");
        VersionControlProvider.setItemName("CL3", "CL3");
        ISmartactorsClassLoader cl3 = VersionControlProvider.getItemClassLoader("CL3");
        VersionControlProvider.addItem("CL4");
        VersionControlProvider.setItemName("CL4", "CL4");
        ISmartactorsClassLoader cl4 = VersionControlProvider.getItemClassLoader("CL4");
        VersionControlProvider.addItem("CL5");
        VersionControlProvider.setItemName("CL5", "CL5");
        ISmartactorsClassLoader cl5 = VersionControlProvider.getItemClassLoader("CL5");
        VersionControlProvider.addItem("CL6");
        VersionControlProvider.setItemName("CL6", "CL6");
        ISmartactorsClassLoader cl6 = VersionControlProvider.getItemClassLoader("CL6");
        VersionControlProvider.addItem("CL7");
        VersionControlProvider.setItemName("CL7", "CL7");
        ISmartactorsClassLoader cl7 = VersionControlProvider.getItemClassLoader("CL7");
        VersionControlProvider.addItemDependency("CL1", "cl00");
        VersionControlProvider.addItemDependency("CL1", "cl01");
        VersionControlProvider.addItemDependency("CL2", "cl01");
        VersionControlProvider.addItemDependency("CL3", "CL1");
        VersionControlProvider.addItemDependency("CL3", "CL2");
        VersionControlProvider.addItemDependency("CL4", "CL2");
        VersionControlProvider.addItemDependency("CL5", "CL4");
        VersionControlProvider.addItemDependency("CL6", "CL5");
        VersionControlProvider.addItemDependency("CL7", "CL6");

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
        for(int i=0; i<iterations+2; i++)
        {
            t1 = System.nanoTime();
            clazz = cl7.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t2 = System.nanoTime();
            clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t3 = System.nanoTime();
            clazz = cl15.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t4 = System.nanoTime();
            if (i>1) {
                d += t2 - t1;
                a += t3 - t2;
                u += t4 - t3;
            }
        }
        d /= iterations;
        a /= iterations;
        u /= iterations;
        System.out.println("Average direct line class reading (ns): "+d);
        System.out.println("Average around line class reading (ns): "+a);
        System.out.println("Average descendant line class reading (ns): "+u);
    }
}