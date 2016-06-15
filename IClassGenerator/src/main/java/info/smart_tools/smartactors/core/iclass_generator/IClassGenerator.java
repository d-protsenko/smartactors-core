package info.smart_tools.smartactors.core.iclass_generator;

import info.smart_tools.smartactors.core.iclass_generator.exception.ClassGenerationException;

/**
 * Interface IClassGenerator
 */
public interface IClassGenerator<T> {

    Class<?> generate(T source, ClassLoader classLoader)
            throws ClassGenerationException;
}
