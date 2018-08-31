package info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api;
import info.smart_tools.smartactors.class_management.class_loader_management.HierarchicalClassLoader;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class for compile string with java code to byte code
 * in memory
 *
 * @since 1.8
 */
class JavacToMemoryCodeCompiler {

    /**
     * System java compiler
     */
    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();

    /**
     * Constructor.
     * Creates instance of {@link JavacToMemoryCodeCompiler}
     */
    private JavacToMemoryCodeCompiler() {}

    /**
     * Compile {@link String} with custom class to java byte code and represent
     * compiled class
     * @param className full name of future class
     * @param classSourceCode code source
     * @param classLoader instance of {@link ClassLoader} to put compiled code to
     * @return compiled class
     * @throws Exception if any errors occurred
     */
    synchronized static Class<?> compile(
            final String className,
            final String classSourceCode,
            final ClassLoader classLoader
    )
            throws Exception {
        HierarchicalClassLoader cl;
        if (null == classLoader) {
            cl = new HierarchicalClassLoader(new URL[]{}, ClassLoader.getSystemClassLoader());
        } else if (classLoader instanceof HierarchicalClassLoader) {
            cl = (HierarchicalClassLoader)classLoader;
        } else {
            cl = new HierarchicalClassLoader(new URL[]{}, classLoader);
        }
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) { }
        try {
            List<String> optionList = new ArrayList<>();
            optionList.addAll(Arrays.asList("-classpath", getClassPath(cl)));
            SourceCode sourceCode = new SourceCode(className, classSourceCode);
            CompiledCode compiledCode = new CompiledCode(className);
            List compilationUnits = Collections.singletonList(sourceCode);
            ExtendedJavaFileManager fileManager = new ExtendedJavaFileManager(
                    javac.getStandardFileManager(null, null, null),
                    compiledCode,
                    cl
            );
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            CompilationTask task = javac.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    optionList,
                    null,
                    compilationUnits
            );
            if (!task.call()) {
                StringBuilder s = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    s
                            .append("\n")
                            .append(diagnostic);
                }
                throw new Exception("Failed to compile " + className + s.toString());

            }
            return cl.addClass(compiledCode.getName(), compiledCode.getByteCode());

        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    /**
     * Append all class paths to given {@link StringBuilder} form given {@link ClassLoader}
     * recursively with given separator
     * @param buf instance of {@link StringBuilder}
     * @param classLoader instance of {@link ClassLoader}
     * @param separator instance of {@link String}
     */
    private static void addClassPathRecursively(StringBuilder buf, final ClassLoader classLoader, String separator) {
        try {
            URLClassLoader ucl = (URLClassLoader) classLoader;

            URL[] urls = ucl.getURLs();
            for (URL url : urls) {
                String jarPathName = url.getFile();
                if (jarPathName.startsWith("file:")) {
                    jarPathName = jarPathName.substring(
                            jarPathName.indexOf("file:") + "file:".length(), jarPathName.indexOf("!/")
                    );
                }
                buf
                        .append(separator)
                        .append(jarPathName);
            }
            try {
                for (ClassLoader dependency : ((HierarchicalClassLoader) ucl).getDependencies()) {
                    addClassPathRecursively(buf, dependency, separator);
                }
            } catch (Exception e) {
                addClassPathRecursively(buf, ucl.getParent(), separator);
            }

        } catch (Exception e) {
            // do nothing
            // because this try-catch check cast ClassLoader to URLClassLoader/HierarchicalClassLoader
        }
    }

    /**
     * Return all class paths as instance of {@link String} form given instance of {@link ClassLoader} recursively
     * @param classLoader instance of {@link ClassLoader}
     * @return all class paths taken from {@link ClassLoader} and its dependencies
     */
    private static String getClassPath(final ClassLoader classLoader) {
        StringBuilder buf = new StringBuilder();
        buf.append(".");
        addClassPathRecursively(buf, classLoader, System.getProperty("path.separator"));
        return buf.toString();
    }
}
