package info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.utility_tool.interfaces.iclass_generator.exception.ClassGenerationException;
import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests for {@link FromStringClassGenerator}
 */
public class FromStringClassGeneratorTest implements TestInterface {

    public Integer getA() {
        return new Integer(1);
    }

    @Test
    public void check()
            throws Exception {
        ExpansibleURLClassLoader cl = new ExpansibleURLClassLoader(
                new URL[]{},
                this.getClass().getClassLoader()
        );
        FromStringClassGenerator classGenerator = new FromStringClassGenerator();
        String testSample = "package info.smart_tools.smartactors.utility_tool.test_class;\n" +
                "import info.smart_tools.smartactors.utility_tool.class_generator_with_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        Class<?> clazz = classGenerator.generate(testSample, cl);
        TestInterface inst = (TestInterface) clazz.newInstance();
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
        ExpansibleURLClassLoader cl = new ExpansibleURLClassLoader(
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
        ExpansibleURLClassLoader cl = new ExpansibleURLClassLoader(
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
}

