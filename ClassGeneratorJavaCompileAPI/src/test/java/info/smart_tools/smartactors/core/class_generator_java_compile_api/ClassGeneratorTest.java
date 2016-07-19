package info.smart_tools.smartactors.core.class_generator_java_compile_api;

import info.smart_tools.smartactors.core.iclass_generator.exception.ClassGenerationException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests for {@link ClassGenerator}
 */
public class ClassGeneratorTest {

    @Test
    public void check()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        String testSample = "package info.smart_tools.smartactors.core.test_class;\n" +
                "import info.smart_tools.smartactors.core.class_generator_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        Class<?> clazz = cl.generate(testSample);
        TestInterface inst = (TestInterface) clazz.newInstance();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnNullArg()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        cl.generate(null);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnEmptyArg()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        cl.generate("");
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTestArgumentWithoutPackage()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        String testSample =
                "import info.smart_tools.smartactors.core.class_generator_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        cl.generate(testSample);
        fail();
    }

    @Test (expected = InvalidArgumentException.class)
    public void checkInvalidArgumentExceptionOnTestArgumentWithoutClass()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        String testSample = "package info.smart_tools.smartactors.core.test_class;\n" +
                "import info.smart_tools.smartactors.core.class_generator_java_compile_api.TestInterface;\n" +
                "public TestClass implements TestInterface {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        cl.generate(testSample);
        fail();
    }

    @Test (expected = ClassGenerationException.class)
    public void checkInvalidArgumentExceptionOnTestWithUndefinedInterface()
            throws Exception {
        ClassGenerator cl = new ClassGenerator(null);
        String testSample = "package info.smart_tools.smartactors.core.test_class;\n" +
                "import info.smart_tools.smartactors.core.class_generator_java_compile_api.TestInterface;\n" +
                "public class TestClass implements TestInterfaceUndefined {\n" +
                "    private int a;\n" +
                "    public Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        cl.generate(testSample);
        fail();
    }
}

