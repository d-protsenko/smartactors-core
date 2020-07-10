package info.smart_tools.smartactors.message_processing.wrapper_generator;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api.FromStringClassGenerator;
import info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api.class_builder.ClassBuilder;
import info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api.class_builder.Modifiers;
import info.smart_tools.smartactors.class_management.interfaces.iclass_generator.IClassGenerator;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.DeleteValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.iobject.iobject.exception.SerializeException;
import info.smart_tools.smartactors.iobject.iobject_wrapper.IObjectWrapper;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.IWrapperGenerator;
import info.smart_tools.smartactors.message_processing_interfaces.iwrapper_generator.exception.WrapperGeneratorException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
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

    /**
     * Constructor.
     * Create new instance of {@link WrapperGenerator} by given {@link ClassLoader}
     */
    public WrapperGenerator() {
        this.classGenerator = new FromStringClassGenerator();
    }

    @Override
    public <T> T generate(final Class<T> targetInterface)
            throws InvalidArgumentException, WrapperGeneratorException {
        T instance = null;

        if (null == targetInterface) {
            throw new InvalidArgumentException("Target class should not be null!");
        }
        if (!targetInterface.isInterface()) {
            throw new InvalidArgumentException("Target class should be an interface!");
        }

        try {
            Class<T> clazz = (Class<T>) targetInterface.getClassLoader().loadClass(
                    targetInterface.getName()+"Impl"
            );
            return clazz.newInstance();

        } catch (Throwable e) {
            // do nothing in case if wrapper implementation class failed to be loaded
            // or new instance of wrapper implementation failed to be created
        }

        try {
            Class<T> clazz = generateClass(targetInterface);

            try {
                return clazz.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("Error on creation instance of wrapper.", e);
            }
        } catch (Throwable e) {
            throw new WrapperGeneratorException(
                    "Could not implement wrapper interface because of the following error:",
                    e
            );
        }
    }

    private <T> Class<T> generateClass(final Class<T> targetInterface)
            throws Exception {

        ClassBuilder cb = new ClassBuilder("\t", "\n");

        // Add package name and imports.
        cb
                .addPackageName(targetInterface.getPackage().getName())
                .addImport(IField.class.getCanonicalName())
                .addImport(Field.class.getCanonicalName())
                .addImport(FieldName.class.getCanonicalName())
                .addImport(InvalidArgumentException.class.getCanonicalName())
                .addImport(targetInterface.getCanonicalName())
                .addImport("info.smart_tools.smartactors.iobject.iobject.IObject")
                .addImport(IObjectWrapper.class.getCanonicalName())
                .addImport(ReadValueException.class.getCanonicalName())
                .addImport(ChangeValueException.class.getCanonicalName())
                .addImport("info.smart_tools.smartactors.iobject.ifield_name.IFieldName")
                .addImport(DeleteValueException.class.getCanonicalName())
                .addImport(SerializeException.class.getCanonicalName())
                .addImport(Iterator.class.getCanonicalName())
                .addImport(Map.class.getCanonicalName())
                .addImport(HashMap.class.getCanonicalName());

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
            cb.addImport((String) entry.getValue());
        }

        // Add class header
        cb
                .addClass()
                        .setClassModifier(Modifiers.PUBLIC)
                        .setClassName(targetInterface.getSimpleName() + "Impl")
                        .setInterfaces("IObjectWrapper")
                        .setInterfaces("IObject")
                        .setInterfaces(targetInterface.getSimpleName());

        // Add class fields
        for (Method m : targetInterface.getMethods()) {
            String methodPrefix;
            if (Void.TYPE == m.getGenericReturnType()) {
                methodPrefix = "out";
            } else {
                methodPrefix = "in";
            }
            cb
                    .addField().setModifier(Modifiers.PRIVATE).setType("IField")
                            .setName("fieldFor_" + methodPrefix + "_" + m.getName());
        }
        cb
                .addField()
                .setModifier(Modifiers.PRIVATE)
                .setType(Map.class.getSimpleName())
                .setInnerGenericType(IFieldName.class.getSimpleName() + "," + Field.class.getSimpleName())
                .setName("fields");

        // Add class constructors
        StringBuilder builder = new StringBuilder();
        builder.append("\t\t").append("try {\n");
        for (Method m : targetInterface.getMethods()) {
            String methodPrefix = Void.TYPE == m.getGenericReturnType() ? "out" : "in";
            builder
                    .append("\t\t\t").append("this.fieldFor_")
                    .append(methodPrefix)
                    .append("_")
                    .append(m.getName())
                    .append(" = new ")
                    .append("Field(new FieldName(\"")
                    .append(methodPrefix).append("_").append(m.getName()).append("\"" + "));\n");
        }
        builder.append("this.fields = new HashMap<>();\n");
        builder
                .append("\t\t").append("} catch (Exception e) {\n").append("\t\t\t")
                .append("throw new InvalidArgumentException(\"\", e);\n")
                .append("\t\t").append("}");
        cb
                .addConstructor().setModifier(Modifiers.PUBLIC).setExceptions("InvalidArgumentException")
                        .addStringToBody(builder.toString());

        // Add 'IObject[] args;' field
        cb
                .addField().setModifier(Modifiers.PRIVATE).setType("IObject").setName("env");
        // Add init method
        cb
                .addMethod().setModifier(Modifiers.PUBLIC).setReturnType("void").setName("init")
                .addParameter()
                .setType("IObject").setName("environments").next()
                .addStringToBody("this.env = environments;");

        // Add 'getIObjects' method
        cb
                .addMethod().setModifier(Modifiers.PUBLIC).setReturnType("IObject").setName("getEnvironmentIObject")
                .addParameter()
                        .setType("IFieldName")
                        .setName("fieldName")
                        .next()
                .setExceptions("InvalidArgumentException")
                .addStringToBody("try {")
                .addStringToBody("\tif (IObjectWrapper.class.isAssignableFrom(this.env.getClass())) {")
                .addStringToBody("\t\treturn ((IObjectWrapper) this.env).getEnvironmentIObject(fieldName);")
                .addStringToBody("\t}")
                .addStringToBody("\treturn (IObject) this.env.getValue(fieldName);")
                .addStringToBody("} catch (Throwable e) {")
                .addStringToBody("throw new InvalidArgumentException(\"Could not get IObject from environments.\", e);")
                .addStringToBody("}");

        // Add getters and setters
        for (Method m : targetInterface.getMethods()) {
            String methodPrefix = Void.TYPE == m.getGenericReturnType() ? "out" : "in";
            if (!m.getReturnType().equals(Void.TYPE)) {
                cb
                        .addMethod().setModifier(Modifiers.PUBLIC).setReturnType(m.getGenericReturnType().getTypeName())
                                .setName(m.getName()).setExceptions("ReadValueException")
                        .addStringToBody("try {")
                        .addStringToBody(
                                "\treturn fieldFor_" +
                                methodPrefix +
                                "_" +
                                m.getName() +
                                ".in(this.env, " + m.getReturnType().getSimpleName() + ".class);"
                        )
                        .addStringToBody("} catch(Throwable e) {")
                        .addStringToBody("\tthrow new ReadValueException(\"Could not get value from iobject.\", e);")
                        .addStringToBody("}");
            } else {
                cb
                        .addMethod().setModifier(Modifiers.PUBLIC).setReturnType("void").setName(m.getName())
                        .setExceptions("ChangeValueException")
                        .addParameter()
                                .setType(m.getGenericParameterTypes()[0].getTypeName()).setName("value").next()
                        .addStringToBody("try {")
                        .addStringToBody(
                                "\tthis.fieldFor_" +
                                methodPrefix +
                                "_" +
                                m.getName() +
                                ".out(this.env, value);"
                        )
                        .addStringToBody("} catch (Throwable e) {")
                        .addStringToBody("\tthrow new ChangeValueException(\"Could not set value from iobject.\", e);")
                        .addStringToBody("}");
            }
        }
        // Add 'IObject' methods

        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .setReturnType(Object.class.getSimpleName())
                .setName("getValue")
                .addParameter()
                .setType(IFieldName.class.getSimpleName())
                .setName("name")
                .next()
                .setExceptions(ReadValueException.class.getSimpleName())
                .setExceptions(InvalidArgumentException.class.getSimpleName())
                .addStringToBody("Field field = fields.get(name);")
                .addStringToBody("if (null == field) {")
                .addStringToBody("\tfield = new Field(name);")
                .addStringToBody("\tfields.put(name, field);")
                .addStringToBody("}")
                .addStringToBody("return new Field(name).in(this.env);");
        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .setReturnType("void")
                .setName("setValue")
                .addParameter()
                .setType(IFieldName.class.getSimpleName())
                .setName("name")
                .next()
                .addParameter()
                .setType(Object.class.getSimpleName())
                .setName("value")
                .next()
                .setExceptions(ChangeValueException.class.getSimpleName())
                .setExceptions(InvalidArgumentException.class.getSimpleName())
                .addStringToBody("Field field = fields.get(name);")
                .addStringToBody("if (null == field) {")
                .addStringToBody("\tfield = new Field(name);")
                .addStringToBody("\tfields.put(name, field);")
                .addStringToBody("}")
                .addStringToBody("new Field(name).out(env, value);");
        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .setReturnType("void")
                .setName("deleteField")
                .addParameter()
                .setType(IFieldName.class.getSimpleName())
                .setName("name")
                .next()
                .setExceptions(DeleteValueException.class.getSimpleName())
                .setExceptions(InvalidArgumentException.class.getSimpleName())
                .addStringToBody("throw new DeleteValueException(\"Method not implemented.\");");
        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .setReturnType("<T> T")
                .setName("serialize")
                .setExceptions(SerializeException.class.getSimpleName())
                .addStringToBody("throw new SerializeException(\"Method not implemented.\");");
        cb
                .addMethod()
                .setModifier(Modifiers.PUBLIC)
                .setReturnType("Iterator<Map.Entry<IFieldName, Object>>")
                .setName("iterator")
                .addStringToBody("return null;");

        return (Class<T>) classGenerator.generate(
                cb.buildClass().toString(),
                targetInterface.getClassLoader()
        );
    }
}


