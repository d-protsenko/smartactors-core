package info.smart_tools.smartactors.class_management.hierarchical_class_loader;

import info.smart_tools.smartactors.class_management.interfaces.ismartactors_class_loader.ISmartactorsClassLoader;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests for SmartactorsClassLoader
 */
public class SmartactorsClassLoaderTest {

    @Test
    public void checkSmartactorsClassLoaderCreation()
            throws Exception {
        SmartactorsClassLoader.addModule(
                "coreId",
                "coreName",
                "coreVersion"
        );
        SmartactorsClassLoader.setDefaultModuleId("coreId");
        SmartactorsClassLoader.addModule("cl1", "cl1", null);
        assertNotNull(SmartactorsClassLoader.getModuleClassLoader("cl1"));
        SmartactorsClassLoader.addModule("cl2", "cl2", null);
        assertNotNull(SmartactorsClassLoader.getModuleClassLoader("cl2"));
        assertSame(SmartactorsClassLoader.getModuleClassLoader("cl2").getParent(), ClassLoader.getSystemClassLoader());
        SmartactorsClassLoader.addModuleDependency("cl1", "cl2");
        SmartactorsClassLoader.finalizeModuleDependencies("cl1");
        SmartactorsClassLoader.finalizeModuleDependencies("cl2");
        assertNotNull(SmartactorsClassLoader.getModuleClassLoader("coreId").getCompilationClassLoader());
    }

    @Test
    public void checkAdditionNewURL()
            throws Exception {
        URL url1 = new URL("http", "host", 9000, "filename1");
        URL url2 = new URL("http", "host", 9000, "filename2");
        URL url3 = new URL("http", "host", 9000, "filename1");
        SmartactorsClassLoader.addModule("cl", "cl", "cl");
        ISmartactorsClassLoader cl0 = SmartactorsClassLoader.getModuleClassLoader("cl");
        assertNotNull(cl0);
        SmartactorsClassLoader.addModule("clURL", "clURL", "clURL");
        ISmartactorsClassLoader cl = SmartactorsClassLoader.getModuleClassLoader("clURL");
        assertNotNull(cl);
        SmartactorsClassLoader.addModuleDependency("clURL", "cl");
        SmartactorsClassLoader.setDefaultModuleId("cl");
        cl0.addURL(url1);
        cl.addURL(url1);
        cl.addURL(url2);
        cl.addURL(url3);
        URL[] result = cl.getURLsFromDependencies();
        ArrayList<URL> al = new ArrayList<URL>();
        for(URL url : result) {
            al.add(url);
        }
        if( !al.contains(url1)) {
            fail();
        }
        if( !al.contains(url2)) {
            fail();
        }

        try {
            Class clazz = cl.loadClass("nowhere.classNotExist");
            fail();
        } catch (ClassNotFoundException e) {
        }

        Class clazz = cl.loadClass("java.lang.String");
    }

    @Test
    public void checkGettersSetters() {

        SmartactorsClassLoader.addModule("cl", "cl", null);
        ISmartactorsClassLoader cl = SmartactorsClassLoader.getModuleClassLoader("cl");

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

    private ClassLoader createClassLoaderForJar(String jarName, ClassLoader parent)
            throws Exception {
        URLClassLoader cl;
        String pathToJar = this.getClass().getClassLoader().getResource(jarName).getFile();
        URL url = new URL("jar:file:" + pathToJar+"!/");
        URL[] urLs = {url};
        cl = new URLClassLoader(urLs, parent);
        return cl;
    }

    private ClassLoader[] createClassLoaderSet()
            throws Exception {
        ClassLoader cls[] = new SmartactorsClassLoader[22];
        SmartactorsClassLoader.addModule("cl00", "DeadEnd-IField", "cl00");
        cls[20] = SmartactorsClassLoader.getModuleClassLoader("cl00");
        SmartactorsClassLoader.addModule("cl01", "PathToSCL-IField", "cl01");
        cls[0] = SmartactorsClassLoader.getModuleClassLoader("cl01");
        SmartactorsClassLoader.addModule("CL1", "CL1", "CL1");
        cls[1] = SmartactorsClassLoader.getModuleClassLoader("CL1");
        SmartactorsClassLoader.addModule("CL2", "CL2", "CL2");
        cls[2] = SmartactorsClassLoader.getModuleClassLoader("CL2");
        SmartactorsClassLoader.addModule("CL3", "CL3", "CL3");
        cls[3] = SmartactorsClassLoader.getModuleClassLoader("CL3");
        SmartactorsClassLoader.addModule("CL4", "CL4", "CL4");
        cls[4] = SmartactorsClassLoader.getModuleClassLoader("CL4");
        SmartactorsClassLoader.addModule("CL5", "CL5", "CL5");
        cls[5] = SmartactorsClassLoader.getModuleClassLoader("CL5");
        SmartactorsClassLoader.addModule("CL6", "CL6", "CL6");
        cls[6] = SmartactorsClassLoader.getModuleClassLoader("CL6");
        SmartactorsClassLoader.addModule("CL7", "CL7", "CL7");
        cls[7] = SmartactorsClassLoader.getModuleClassLoader("CL7");

        SmartactorsClassLoader.addModuleDependency("CL1", "cl00");
        SmartactorsClassLoader.addModuleDependency("CL1", "cl01");
        SmartactorsClassLoader.addModuleDependency("CL2", "cl01");
        SmartactorsClassLoader.addModuleDependency("CL3", "CL1");
        SmartactorsClassLoader.addModuleDependency("CL3", "CL2");
        SmartactorsClassLoader.addModuleDependency("CL4", "CL2");
        SmartactorsClassLoader.addModuleDependency("CL5", "CL4");
        SmartactorsClassLoader.addModuleDependency("CL6", "CL5");
        SmartactorsClassLoader.addModuleDependency("CL7", "CL6");

        attachJarToClassLoader("ifield.jar", (ISmartactorsClassLoader)cls[20]);
        attachJarToClassLoader("ifield.jar", (ISmartactorsClassLoader)cls[0]);
        attachJarToClassLoader("ifield_name.jar", (ISmartactorsClassLoader)cls[2]);
        attachJarToClassLoader("iobject.jar", (ISmartactorsClassLoader)cls[2]);
        attachJarToClassLoader("iobject-wrapper.jar", (ISmartactorsClassLoader)cls[2]);

        return cls;
    }

    @Test
    public void performanceTestForExtendedClassLoader()
            throws Exception {
        ClassLoader[] cls;

        Class[] clazz = { null, null, null, null, null };
        long iterations, t1, t2, t3, t4;
        double d, a, u;
        d = 0; a = 0; u = 0;
        iterations = 1000;
        for(int i=0; i<iterations+5; i++)
        {
            cls = createClassLoaderSet();

            t1 = System.nanoTime();
            clazz[0] = cls[2].loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
            t2 = System.nanoTime();
            clazz[1] = cls[3].loadClass("info.smart_tools.smartactors.iobject.iobject.IObject");
            t3 = System.nanoTime();
            if (i>4 && clazz[0].getClassLoader().getParent() != null && clazz[1].getClassLoader().getParent() != null) {
                d += t2 - t1;
                a += t3 - t2;
            }
        }
        d /= iterations;
        a /= iterations;
        System.out.println("");
        System.out.println("Average direct line class loading (ns): "+d);
        System.out.println("Average aroung line class loading (ns): "+a);

        cls = createClassLoaderSet();

        ClassLoader cls8 =  createClassLoaderForJar("ifield.jar", ClassLoader.getSystemClassLoader());
        ClassLoader cls9 =  createClassLoaderForJar("ifield_name.jar", cls8);
        ClassLoader cls10 = createClassLoaderForJar("iobject.jar", cls9);
        ClassLoader cls11 = createClassLoaderForJar("iobject-wrapper.jar", cls10);
        ClassLoader cls12 = new URLClassLoader(new URL[]{}, cls11);
        ClassLoader cls13 = new URLClassLoader(new URL[]{}, cls12);
        ClassLoader cls14 = new URLClassLoader(new URL[]{}, cls13);
        ClassLoader cls15 = new URLClassLoader(new URL[]{}, cls14);

        clazz[0] = cls[2].loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
        clazz[1] = cls[3].loadClass("info.smart_tools.smartactors.iobject.iobject.IObject");
        clazz[2] = cls[3].loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        clazz[3] = cls[7].loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        clazz[4] = cls15 .loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        d = 0; a = 0; u = 0;
        iterations = 100000;
        for(int i=0; i<iterations+2; i++)
        {
            t1 = System.nanoTime();
            clazz[0] = cls[7].loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t2 = System.nanoTime();
            clazz[1] = cls[3].loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t3 = System.nanoTime();
            clazz[2] = cls15 .loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t4 = System.nanoTime();
            if (i>1 &&
                    clazz[0].getClassLoader().getParent() != null &&
                    clazz[1].getClassLoader().getParent() != null &&
                    clazz[2].getClassLoader().getParent() != null
            ) {
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