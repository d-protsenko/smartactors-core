package info.smart_tools.smartactors.class_management.version_manager;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * Tests for SmartactorsClassLoader
 */
public class VersionManagerTest {

    @Test
    public void checkSmartactorsClassLoaderCreation()
            throws Exception {
        VersionManager.addModule(VersionManager.coreId, VersionManager.coreName, VersionManager.coreVersion);
        VersionManager.addModule("cl1", "cl1", null);
        assertNotNull(VersionManager.getModuleClassLoader("cl1"));
        VersionManager.addModule("cl2", "cl2", null);
        assertNotNull(VersionManager.getModuleClassLoader("cl2"));
        assertSame(((ClassLoader) VersionManager.getModuleClassLoader("cl2")).getParent(), ClassLoader.getSystemClassLoader());
        VersionManager.addModuleDependency("cl1", "cl2");
        VersionManager.finalizeModuleDependencies("cl1", VersionManager.coreId);
        VersionManager.finalizeModuleDependencies("cl2", VersionManager.coreId);
    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
        URL url1 = new URL("http", "host", 9000, "filename1");
        URL url2 = new URL("http", "host", 9000, "filename2");
        URL url3 = new URL("http", "host", 9000, "filename1");
        VersionManager.addModule("clURL", "clURL", "clURL");
        ISmartactorsClassLoader cl = VersionManager.getModuleClassLoader("clURL");
        assertNotNull(cl);

        cl.addURL(url1);
        cl.addURL(url2);
        cl.addURL(url3);
        URL[] result = cl.getURLsFromDependencies();
        assertSame(url1, result[result.length-2]);
        assertSame(url2, result[result.length-1]);

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

        VersionManager.addModule("cl", "cl", null);
        ISmartactorsClassLoader cl = VersionManager.getModuleClassLoader("cl");

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
            VersionManager.addModule("cl00", "DeadEnd-IField", "cl00");
            ISmartactorsClassLoader cl00 = VersionManager.getModuleClassLoader("cl00");
            VersionManager.addModule("cl01", "PathToSCL-IField", "cl01");
            ISmartactorsClassLoader cl01 = VersionManager.getModuleClassLoader("cl01");
            VersionManager.addModule("CL1", "CL1", "CL1");
            ISmartactorsClassLoader cl1 = VersionManager.getModuleClassLoader("CL1");
            VersionManager.addModule("CL2", "CL2", "CL2");
            ISmartactorsClassLoader cl2 = VersionManager.getModuleClassLoader("CL2");
            VersionManager.addModule("CL3", "CL3", "CL3");
            ISmartactorsClassLoader cl3 = VersionManager.getModuleClassLoader("CL3");
            VersionManager.addModule("CL4", "CL4", "CL4");
            ISmartactorsClassLoader cl4 = VersionManager.getModuleClassLoader("CL4");
            VersionManager.addModuleDependency("CL1", "cl00");
            VersionManager.addModuleDependency("CL1", "cl01");
            VersionManager.addModuleDependency("CL2", "cl01");
            VersionManager.addModuleDependency("CL3", "CL1");
            VersionManager.addModuleDependency("CL3", "CL2");
            VersionManager.addModuleDependency("CL4", "CL2");
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
            VersionManager.addModule("cl00", "DeadEnd-IField", "cl00");
            ISmartactorsClassLoader cl00 = VersionManager.getModuleClassLoader("cl00");
            VersionManager.addModule("cl01", "PathToSCL-IField", "cl01");
            ISmartactorsClassLoader cl01 = VersionManager.getModuleClassLoader("cl01");
            VersionManager.addModule("CL1", "CL1", "CL1");
            ISmartactorsClassLoader cl1 = VersionManager.getModuleClassLoader("CL1");
            VersionManager.addModule("CL2", "CL2", "CL2");
            ISmartactorsClassLoader cl2 = VersionManager.getModuleClassLoader("CL2");
            VersionManager.addModule("CL3", "CL3", "CL3");
            ISmartactorsClassLoader cl3 = VersionManager.getModuleClassLoader("CL3");
            VersionManager.addModule("CL4", "CL4", "CL4");
            ISmartactorsClassLoader cl4 = VersionManager.getModuleClassLoader("CL4");
            VersionManager.addModuleDependency("CL1", "cl00");
            VersionManager.addModuleDependency("CL1", "cl01");
            VersionManager.addModuleDependency("CL2", "cl01");
            VersionManager.addModuleDependency("CL3", "CL1");
            VersionManager.addModuleDependency("CL3", "CL2");
            VersionManager.addModuleDependency("CL4", "CL2");

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

        VersionManager.addModule("cl00", "DeadEnd-IField", "cl00");
        ISmartactorsClassLoader cl00 = VersionManager.getModuleClassLoader("cl00");
        VersionManager.addModule("cl01", "PathToSCL-IField", "cl01");
        ISmartactorsClassLoader cl01 = VersionManager.getModuleClassLoader("cl01");
        VersionManager.addModule("CL1", "CL1", "CL1");
        ISmartactorsClassLoader cl1 = VersionManager.getModuleClassLoader("CL1");
        VersionManager.addModule("CL2", "CL2", "CL2");
        ISmartactorsClassLoader cl2 = VersionManager.getModuleClassLoader("CL2");
        VersionManager.addModule("CL3", "CL3", "CL3");
        ISmartactorsClassLoader cl3 = VersionManager.getModuleClassLoader("CL3");
        VersionManager.addModule("CL4", "CL4", "CL4");
        ISmartactorsClassLoader cl4 = VersionManager.getModuleClassLoader("CL4");
        VersionManager.addModule("CL5", "CL5", "CL5");
        ISmartactorsClassLoader cl5 = VersionManager.getModuleClassLoader("CL5");
        VersionManager.addModule("CL6", "CL6", "CL6");
        ISmartactorsClassLoader cl6 = VersionManager.getModuleClassLoader("CL6");
        VersionManager.addModule("CL7", "CL7", "CL7");
        ISmartactorsClassLoader cl7 = VersionManager.getModuleClassLoader("CL7");
        VersionManager.addModuleDependency("CL1", "cl00");
        VersionManager.addModuleDependency("CL1", "cl01");
        VersionManager.addModuleDependency("CL2", "cl01");
        VersionManager.addModuleDependency("CL3", "CL1");
        VersionManager.addModuleDependency("CL3", "CL2");
        VersionManager.addModuleDependency("CL4", "CL2");
        VersionManager.addModuleDependency("CL5", "CL4");
        VersionManager.addModuleDependency("CL6", "CL5");
        VersionManager.addModuleDependency("CL7", "CL6");

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