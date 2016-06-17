package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.class_generator_java_compile_api.ClassGenerator;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.iclass_generator.IClassGenerator;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.core.iwrapper_generator.exception.WrapperGeneratorException;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IWrapperGenerator}.
 * <pre>
 * Main features of implementation:
 * - use class generator based on java compile API;
 * - use supporting class Field;
 * </pre>
 */
public class WrapperGenerator implements IWrapperGenerator {

    private IClassGenerator<String> classGenerator;

    public WrapperGenerator(final ClassLoader classLoader) {
        this.classGenerator = new ClassGenerator(classLoader);
    }

    @Override
    public <T> T generate(final Class<T> targetInterface, final Map<String, String> binding)
            throws InvalidArgumentException, WrapperGeneratorException {
        T instance = null;

        if (null == targetInterface) {
            throw new InvalidArgumentException("Target class must not be null!");
        }
        if (!targetInterface.isInterface()) {
            throw new InvalidArgumentException("Target class must be an interface!");
        }

        try {
            instance = IOC.resolve(Keys.getOrAdd(targetInterface.getClass().toString()));
        } catch (ResolutionException e) {
            // do nothing
        }
        if (null != instance) {
            return instance;
        }

        try {
            Class<T> clazz = generateClass(targetInterface, binding);
            // May be later CreateNewInstanceStrategy will be replaced by GetInstanceFromPoolStrategy
            IOC.register(
                    Keys.getOrAdd(targetInterface.getClass().toString()),
                    new CreateNewInstanceStrategy(
                            (arg) ->  {
                                try {
                                    return clazz.newInstance();
                                } catch (Throwable e) {
                                    throw new RuntimeException("Error on creation new instance.", e);
                                }
                            }
                    )
            );

            return IOC.resolve(Keys.getOrAdd(targetInterface.getClass().toString()));
        } catch (Throwable e) {
            throw new WrapperGeneratorException(
                    "Could not implement wrapper interface because of the following error:",
                    e
            );
        }
    }

    private <T> Class<T> generateClass(final Class<T> targetInterface, final Map<String, String> binding)
            throws Exception {
        StringBuilder buf = new StringBuilder();

        // Add package name
        buf.append("package ").append(targetInterface.getPackage().getName()).append("; \n");
        // Add imports
        buf.append("import ").append(targetInterface.getCanonicalName()).append(";\n");
        Map<Class<?>, String> types = new HashMap<>();
        for (Method m : targetInterface.getMethods()) {
            Class<?> returnType = m.getReturnType();
            if (!returnType.isPrimitive()) {
                types.put(returnType, returnType.getCanonicalName());
            }
            Class<?>[] args = m.getParameterTypes();
            for (Class<?> c : args) {
                if (!c.isPrimitive()) {
                    types.put(c, c.getCanonicalName());
                }
            }
        }
        for (Object o : types.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            buf.append("import ").append(entry.getValue()).append(";\n");
        }

        // Add 'public class targetInterfaceImpl implements IObjectWrapper {'
        buf
                .append("public class ")
                .append(targetInterface.getSimpleName())
                .append("Impl ")
                .append("implements IObjectWrapper { \n");
        // Add private field - message
        buf
                .append("\t")
                .append("private IObject message;\n");
        // Add getter for message
        buf
                .append("\t")
                .append("public IObject getMessage() {\n")
                .append("\t\t")
                .append("return message;\n")
                .append("\t}").append("\n");
        // Add private field - context
        buf
                .append("\t")
                .append("private IObject context;\n");
        // Add getter for context
        buf
                .append("\t")
                .append("public IObject getContext() {\n")
                .append("\t\t")
                .append("return context;\n")
                .append("\t}").append("\n");
        // Add private field - response
        buf
                .append("\t")
                .append("private IObject response;\n");
        // Add getter for response
        buf
                .append("\t")
                .append("public IObject getResponse() {\n")
                .append("\t\t")
                .append("return response;\n")
                .append("\t}").append("\n");
        // Add implementation for 'init' method
        buf
                .append("\t")
                .append("public void init(IObject message, IObject context, IObject response) {\n")
                .append("\t\t")
                .append("this.message = message;\n")
                .append("\t\t")
                .append("this.context = context;\n")
                .append("\t\t")
                .append("this.response = response;\n")
                .append("\t}\n");

        for (Method m : targetInterface.getMethods()) {
            Type[] argsTypes = m.getGenericParameterTypes();
            Type returnType = m.getGenericReturnType();

        }

        // Add end of class
        buf.append("}");


        return (Class<T>) classGenerator.generate(buf.toString());
    }
}


