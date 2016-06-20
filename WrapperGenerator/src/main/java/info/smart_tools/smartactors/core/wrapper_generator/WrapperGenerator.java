package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.class_generator_java_compile_api.ClassGenerator;
import info.smart_tools.smartactors.core.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.core.ds_object.FieldName;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link IWrapperGenerator}.
 * <pre>
 * Main features of implementation:
 * - use class generator based on java compile API;
 * - use supporting class Field;
 * </pre>
 */
public class WrapperGenerator implements IWrapperGenerator {

    private static Pattern fieldNameSeparationPattern = Pattern.compile("(get|is|has|set|count)");
    private static Pattern sourceSelectorPattern = Pattern.compile("(message.|context.|response.)");

    private static Map<String, Function<Method, String>> writersForMethods = new HashMap<>();
    private static Map<String, Function<Method, String>> writersForFields = new HashMap<>();
    private static Map<String, String> mapPrimitiveToClass = new HashMap<>();
    static {
        mapPrimitiveToClass.put(int.class.getName(), "Integer");
        mapPrimitiveToClass.put(long.class.getName(), "Long");
        mapPrimitiveToClass.put(float.class.getName(), "Float");
        mapPrimitiveToClass.put(double.class.getName(), "Double");
        mapPrimitiveToClass.put(byte.class.getName(), "Byte");
        mapPrimitiveToClass.put(boolean.class.getName(), "Boolean");
        mapPrimitiveToClass.put(char.class.getName(), "Character");
        mapPrimitiveToClass.put(short.class.getName(), "Short");
    }
    static {
        writersForMethods.put("get", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type returnType = m.getGenericReturnType();
            builder
                    .append("\t")
                    .append("public ")
                    .append(returnType.getTypeName())
                    .append(" ")
                    .append(m.getName())
                    .append("() {\n")
                    .append("\t\t")
                    .append("try {\n")
                    .append("\t\t\t")
                    .append("return fieldFor_")
                    .append(m.getName())
                    .append(".from(")
                    // TODO change to message, context, response
                    .append("message")
                    .append(");\n")
                    .append("\t\t")
                    .append("} catch(Throwable e) { \n")
                    .append("\t\t\t")
                    .append("throw new RuntimeException(\"Could not get value from iobject.\", e);\n")
                    .append("\t\t")
                    .append("\t\t")
                    .append("}\n")
                    .append("\t}\n");

            return builder.toString();
        });

        writersForMethods.put("has", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type returnType = m.getGenericReturnType();
            builder
                    .append("\t")
                    .append("public ")
                    .append(returnType.getTypeName())
                    .append(" ")
                    .append(m.getName())
                    .append("() {\n")
                    .append("\t\t")
                    .append("return null;\n")
                    .append("\t}\n");

            return builder.toString();
        });
        writersForMethods.put("is", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type returnType = m.getGenericReturnType();
            builder
                    .append("\t")
                    .append("public ")
                    .append(returnType.getTypeName())
                    .append(" ")
                    .append(m.getName())
                    .append("() {\n")
                    .append("\t\t")
                    .append("return null;\n")
                    .append("\t}\n");

            return builder.toString();
        });
        writersForMethods.put("set", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type[] args = m.getGenericParameterTypes();
            builder
                    .append("\t")
                    .append("public void")
                    .append(" ")
                    .append(m.getName())
                    .append("(")
                    .append(args[0].getTypeName())
                    .append(" value")
                    .append(") {\n")
                    .append("\t}\n");

            return builder.toString();
        });
        writersForMethods.put("count", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type returnType = m.getGenericReturnType();
            builder
                    .append("\t")
                    .append("public ")
                    .append(returnType.getTypeName())
                    .append(" ")
                    .append(m.getName())
                    .append("() {\n")
                    .append("\t\t")
                    .append("return null;\n")
                    .append("\t}\n");

            return builder.toString();
        });
        writersForFields.put("void", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type[] parametersTypes = m.getGenericParameterTypes();
            builder
                    .append("\t")
                    .append("private Field<")
                    .append(
                            null != mapPrimitiveToClass.get(parametersTypes[0].getTypeName()) ?
                            mapPrimitiveToClass.get(parametersTypes[0].getTypeName())    :
                            parametersTypes[0].getTypeName())
                    .append("> ")
                    .append("fieldFor_")
                    .append(m.getName())
                    .append(";\n");

            return builder.toString();
        });
        writersForFields.put("notvoid", (m) -> {
            StringBuilder builder = new StringBuilder();
            Type returnType = m.getGenericReturnType();
            builder
                    .append("\t")
                    .append("private Field<")
                    .append(
                            null != mapPrimitiveToClass.get(returnType.getTypeName()) ?
                            mapPrimitiveToClass.get(returnType.getTypeName())    :
                            returnType.getTypeName())
                    .append("> ")
                    .append("fieldFor_")
                    .append(m.getName())
                    .append(";\n");

            return builder.toString();
        });
    }

    private IClassGenerator<String> classGenerator;

    /**
     * Constructor.
     * Create new instance of {@link WrapperGenerator} by given {@link ClassLoader}
     * @param classLoader the instance of {@link ClassLoader}
     */
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


            //Tests





            // May be later CreateNewInstanceStrategy will be replaced by GetInstanceFromPoolStrategy
//            IOC.register(
//                    Keys.getOrAdd(targetInterface.getClass().toString()),
//                    new CreateNewInstanceStrategy(
//                            (arg) ->  {
//                                try {
//                                    return clazz.newInstance();
//                                } catch (Throwable e) {
//                                    throw new RuntimeException("Error on creation new instance.", e);
//                                }
//                            }
//                    )
//            );
//
//            return IOC.resolve(Keys.getOrAdd(targetInterface.getClass().toString()));


            return clazz.newInstance();

        } catch (Throwable e) {
            throw new WrapperGeneratorException(
                    "Could not implement wrapper interface because of the following error:",
                    e
            );
        }
    }

    private <T> Class<T> generateClass(final Class<T> targetInterface, final Map<String, String> binding)
            throws Exception {
        StringBuilder builder = new StringBuilder();

        // Add package name
        builder.append("package ").append(targetInterface.getPackage().getName()).append(";\n");
        // Add imports
        builder.append("import ").append(Field.class.getCanonicalName()).append(";\n");
        builder.append("import ").append(Method.class.getCanonicalName()).append(";\n");
        builder.append("import ").append(FieldName.class.getCanonicalName()).append(";\n");
        builder.append("import ").append(InvalidArgumentException.class.getCanonicalName()).append(";\n");
        builder.append("import ").append(targetInterface.getCanonicalName()).append(";\n");
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
            builder.append("import ").append(entry.getValue()).append(";\n");
        }

        // Add 'public class targetInterfaceImpl implements IObjectWrapper {'
        builder
                .append("public class ")
                .append(targetInterface.getSimpleName())
                .append("Impl ")
                .append("implements IObjectWrapper, ")
                .append(targetInterface.getSimpleName())
                .append(" { \n");
        // Add private Field properties
        for (Method m : targetInterface.getMethods()) {
            if (Void.TYPE == m.getGenericReturnType()) {
                builder.append(writersForFields.get("void").apply(m));
            } else {
                builder.append(writersForFields.get("notvoid").apply(m));
            }
        }

        //Add default constructor
        builder
                .append("\t")
                .append("public ")
                .append(targetInterface.getSimpleName())
                .append("Impl")
                .append("() throws InvalidArgumentException {\n");
        builder
                .append("\t\t")
                .append("try {\n");
        for (Method m : targetInterface.getMethods()) {
            builder
                    .append("\t\t\t")
                    .append("this.fieldFor_")
                    .append(m.getName())
                    .append(" = new Field<>(new FieldName(\"")
                    .append(sourceSelectorPattern.matcher(binding.get(m.getName())).replaceAll(""))
                    .append("\"), ")
                    .append(targetInterface.getSimpleName())
                    .append(".class.getMethod(\"")
                    .append(m.getName())
                    .append("\", ")
                    .append(m.getParameterTypes()[0].getName())
                    .append(")")
                    .append(");\n");
        }
        builder
                .append("\t\t")
                .append("} catch (Exception e) {\n")
                .append("\t\t\t")
                .append("throw new InvalidArgumentException(\"\", e);\n")
                .append("\t\t")
                .append("}\n");
        builder.append("}\n");

        // Add private field - message
        builder
                .append("\t")
                .append("private IObject message;\n");
        // Add getter for message
        builder
                .append("\t")
                .append("public IObject getMessage() {\n")
                .append("\t\t")
                .append("return message;\n")
                .append("\t}").append("\n");
        // Add private field - context
        builder
                .append("\t")
                .append("private IObject context;\n");
        // Add getter for context
        builder
                .append("\t")
                .append("public IObject getContext() {\n")
                .append("\t\t")
                .append("return context;\n")
                .append("\t}").append("\n");
        // Add private field - response
        builder
                .append("\t")
                .append("private IObject response;\n");
        // Add getter for response
        builder
                .append("\t")
                .append("public IObject getResponse() {\n")
                .append("\t\t")
                .append("return response;\n")
                .append("\t}").append("\n");
        // Add implementation for 'init' method
        builder
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
            Matcher matcher = fieldNameSeparationPattern.matcher(m.getName());
            if (matcher.find()) {
                builder.append(writersForMethods.get(matcher.group(1)).apply(m));
            }


        }

        // Add end of class
        builder.append("}");


        return (Class<T>) classGenerator.generate(builder.toString());
    }



}


