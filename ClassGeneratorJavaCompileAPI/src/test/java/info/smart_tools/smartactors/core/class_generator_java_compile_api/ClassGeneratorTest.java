package info.smart_tools.smartactors.core.class_generator_java_compile_api;

import org.junit.Test;

/**
 * Tests for {@link ClassGenerator}
 */
public class ClassGeneratorTest {

    @Test
    public void check()
            throws Exception {
        ClassGenerator cl = new ClassGenerator();
        String testSample = "package info.smart_tools.smartactors.core.test_class;\n" +
                "public class TestClass{\n" +
                "    private int a;\n" +
                "    Integer getA() {\n" +
                "        return a;\n" +
                "    }\n" +
                "}\n";
        cl.generate(testSample, ClassLoader.getSystemClassLoader());
    }
}