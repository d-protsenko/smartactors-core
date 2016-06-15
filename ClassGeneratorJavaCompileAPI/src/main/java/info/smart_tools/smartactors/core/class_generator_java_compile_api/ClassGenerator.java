package info.smart_tools.smartactors.core.class_generator_java_compile_api;

import info.smart_tools.smartactors.core.iclass_generator.IClassGenerator;
import info.smart_tools.smartactors.core.iclass_generator.exception.ClassGenerationException;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link IClassGenerator}
 */
public class ClassGenerator implements IClassGenerator<String> {

    @Override
    public Class<?> generate(final String source, final ClassLoader classLoader) throws ClassGenerationException {
        try {

            String packageName = getFirstSubstringByPattern(source, Pattern.compile("package (.*?);"));
            String className = getFirstSubstringByPattern(source, Pattern.compile("class (.*?)(\\s)|(\\{)}"));
            String fullClassName = packageName + "." + className;
            return InMemoryCodeCompiler.compile(getClassPath(classLoader), fullClassName, source);
        } catch (Throwable e) {
            throw new ClassGenerationException("Could not generate class.", e);
        }
    }

    private String getClassPath(final ClassLoader classLoader) {
        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        StringBuilder buf = new StringBuilder();
        buf.append(".");
        String separator = System.getProperty("path.separator");
        for (URL url : urls) {
            buf.append(separator).append(url.getFile());
        }

        return buf.toString();
    }

    private String getFirstSubstringByPattern(final String source, final Pattern pattern) {
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}