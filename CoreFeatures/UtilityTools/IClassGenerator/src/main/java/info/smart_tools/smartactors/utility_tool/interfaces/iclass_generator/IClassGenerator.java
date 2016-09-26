package info.smart_tools.smartactors.utility_tool.interfaces.iclass_generator;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.utility_tool.interfaces.iclass_generator.exception.ClassGenerationException;

/**
 * Interface IClassGenerator
 * @param <T> type of code source
 */
public interface IClassGenerator<T> {

    /**
     * Compile given source to java class
     * @param source source code for compilation
     * @return compiled class
     * @throws ClassGenerationException if any errors occurred
     * @throws InvalidArgumentException if arguments are incorrect
     */
    Class<?> generate(T source)
            throws ClassGenerationException, InvalidArgumentException;
}
