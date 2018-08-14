package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.utility_tool.interfaces.iclass_generator.exception.ClassGenerationException;
import org.junit.Test;

import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link FromStringClassGenerator}
 */
public class FromStringClassGeneratorTest {

    @Test
    public void check()
            throws Exception {
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample = "package info.smart_tools.smartactors.utility_tool.test_class;\n" +
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        Class<?> clazz = classGenerator.generate(testSample, null);
        TestInterface inst = (TestInterface) clazz.newInstance();
        clazz = classGenerator.generate(testSample, getClass().getClassLoader());
        inst = (TestInterface) clazz.newInstance();
        clazz = classGenerator.generate(testSample, new ExtendedURLClassLoader(new URL[]{}, getClass().getClassLoader()));
        inst = (TestInterface) clazz.newInstance();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullArg()
            throws Exception {
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        classGenerator.generate(null, this.getClass().getClassLoader());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnEmptyArg()
            throws Exception {
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        classGenerator.generate("", this.getClass().getClassLoader());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTestArgumentWithoutPackage()
            throws Exception {
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample =
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        classGenerator.generate(testSample, this.getClass().getClassLoader());
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTestArgumentWithoutClass()
            throws Exception {
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample = "package info.smart_tools.smartactors.utility_tool.test_class;\n" +
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_apiTestInterface;\n" +
                "public TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        classGenerator.generate(testSample, this.getClass().getClassLoader());
        fail();
    }

    @Test (expected = ClassGenerationException.class)
    public void checkInvalidArgumentExceptionOnTestWithUndefinedInterface()
            throws Exception {
        ExtendedURLClassLoader cl = new ExtendedURLClassLoader(
                new URL[]{},
                this.getClass().getClassLoader()
        );
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample = "package info.smart_tools.smartactors.utility_tool.test_class;\n" +
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterfaceUndefined {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        Class clazz = classGenerator.generate(testSample, cl);
        fail();
    }

    @Test
    public void checkClassGenerationWithUsingJarFile()
            throws Exception {

        String pathToJar = this.getClass().getClassLoader().getResource("ifield_name.jar").getFile();
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> e = jarFile.entries();

        URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
        ExtendedURLClassLoader cl = new ExtendedURLClassLoader(
                urls,
                this.getClass().getClassLoader()
        );
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class")){
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0,je.getName().length()-6);
            className = className.replace('/', '.');
            Class c = cl.loadClass(className);

        }

        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample = "package info.smart_tools.smartactors.utility_tool.test_class;\n" +
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;\n" +
                "import info.smart_tools.smartactors.core.ifield_name.IFieldName;\n" +
                "public class TestClass implements TestInterface, IFieldName {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        Class newClass = classGenerator.generate(testSample, cl);
        assertNotNull(newClass);
    }

    private void attachJarToClassLoader(String jarName, ExtendedURLClassLoader cl)
            throws Exception {
        String pathToJar = this.getClass().getClassLoader().getResource(jarName).getFile();
        cl.addUrl(new URL("jar:file:" + pathToJar+"!/"));
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
        long iterations, t1, t2, t3;
        double d, a;
        d = 0;
        a = 0;
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
        clazz = cl2.loadClass("info.smart_tools.smartactors.iobject.ifield.IField");
        clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject.IObject");
        clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        clazz = cl7.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
        d = 0;
        a = 0;
        iterations = 100000;
        for(int i=0; i<iterations; i++)
        {
            t1 = System.nanoTime();
            clazz = cl7.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t2 = System.nanoTime();
            clazz = cl3.loadClass("info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper");
            t3 = System.nanoTime();
            d += t2-t1;
            a += t3-t2;
        }
        d /= iterations;
        a /= iterations;
        System.out.println("Average direct line class reading (ns): "+d);
        System.out.println("Average aroung line class reading (ns): "+a);

    }
}

