package info.smart_tools.smartactors.core.proof_of_assumption.old_generator;

import info.smart_tools.smartactors.core.ds_object.FieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ObjectWrapperGenerator {

    private static Map<String, String> mapPrimitiveToClass = new HashMap<>(8);
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
    private static Map<String, String> mapPrimitivePostfixes = new HashMap<>(8);
    static {
        mapPrimitivePostfixes.put(int.class.getName(), ").intValue()");
        mapPrimitivePostfixes.put(long.class.getName(), ").longValue()");
        mapPrimitivePostfixes.put(boolean.class.getName(), ").booleanValue()");
        mapPrimitivePostfixes.put(byte.class.getName(), ").byteValue()");
        mapPrimitivePostfixes.put(float.class.getName(), ").floatValue()");
        mapPrimitivePostfixes.put(double.class.getName(), ").doubleValue()");
        mapPrimitivePostfixes.put(char.class.getName(), ").charValue()");
        mapPrimitivePostfixes.put(short.class.getName(), ").shortValue()");
    }
    private static Properties properties = new Properties();
    static {
        try(InputStream stream = ObjectWrapperGenerator.class
                .getResourceAsStream("objectWrapperGenerator.properties")) {
            properties.load(stream);
        } catch (IOException e) {
        }
    }

    /**
     * Generates wrapper class for IObject that implements given interface.
     * Each getter of resulting class will return converted with Field field of IObject.
     * Each setter of resulting class will set named value in IObject.
     * Example:
     * <pre>
     * {@code
     * Given:
     *      public interface SomeInterface extends IObjectWrapper {
     *          long getNumber();
     *          String getName();
     *          void setResult(boolean result);
     *          IObject getSection(int index);
     *          int countSection();
     *          void setSection(Iterable<IObject> iterable);
     *      }
     *
     * Generated:
     *      public class GeneratedSomeInterfaceImpl implements SomeInterface {
     *
     *          private IObject wrapped;
     *          private Field _number;
     *          private Field _name;
     *          private Field _result;
     *          private ListField _section;
     *
     *          public SomeInterfaceImpl() {
     *              this._number = new Field(new FieldName("number"));
     *              this._name = new Field(new FieldName("name"));
     *              this._result = new Field(new FieldName("result"));
     *              this._section = new ListField(new FieldName("section"));
     *          }
     *
     *          public long getNumber() {
     *              return (long) ((Long) _number.from(wrapped, Long.class)).longValue();
     *          }
     *
     *          public String getName() {
     *              return (String) _name.from(wrapped, String.class));
     *          }
     *
     *          public void setResult(boolean result) {
     *              _result.inject(wrapped, result);
     *          }
     *
     *          public IObject getSection(int index) {
     *               return _section.from(this.wrapped, IObject.class).get(index);
     *           }
     *
     *          public int countSection() {
     *              return _section.from(this.wrapped, Object.class).size();
     *          }
     *
     *          public void setSection(Iterable<IObject> iterable) {
     *              _section.inject(this.wrapped, CollectionUtils.newArrayList(iterable));
     *          }
     *
     *          IObject extractWrapped() {
     *              return wrapped;
     *          }
     *
     *          void init(IObject object) {
     *              this.wrapped = object;
     *          }
     *      }
     * }
     * </pre>
     *
     * @param target is class of interface, which will be implemented
     * @param <T> is type parameter of argument
     * @return {@code Class<? extends T>} implementation of target interface
     * @throws IllegalArgumentException if target == null or
     *                                  if target is not an interface
     * @throws GeneratorException if class can not be generated
     */
    public <T> Class<? extends T> generateWrapperFor(Class<T> target) throws GeneratorException {
        if (target == null) {
            throw new IllegalArgumentException("Target interface must not be null!");
        }
        if (!target.isInterface()) {
            throw new IllegalArgumentException("Target class must be an interface!");
        }

        try {
            CtClass resultClass = getCtClass(target);

            StringBuilder constructorBuilder = new StringBuilder();
            for (Method m : target.getDeclaredMethods()) {
                //regExp splits camelcase
                //IWrapperMethodHandler handler = wrpMethodHandler.get(m.getName().split("(?<!^)(?=[A-Z])")[0]);
                handleInterfaceMethod(resultClass, m, constructorBuilder);
            }
            handleWrapperSpecialMethods(resultClass);
            addConstructor(resultClass, constructorBuilder.toString());

            Class<? extends T> toClass = resultClass.toClass();
            return toClass;
        } catch (CannotCompileException | NotFoundException | NoSuchMethodException e) {
            throw new GeneratorException(e);
        }
    }

    /**
     * Constructs class with default constructor, interface and field from given class
     * @param target given class
     * @param <T> type of the given class
     * @return constructed class
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    private static <T> CtClass getCtClass(Class<T> target) throws NotFoundException, CannotCompileException {
        ClassPool pool = ClassPool.getDefault();
        CtClass targetInterface = pool.get(target.getName());

        CtClass resultClass = pool.makeClass(MessageFormat
                .format(properties.getProperty("clazz.name"),
                        target.getPackage().getName(),
                        target.getSimpleName()));
        resultClass.addInterface(targetInterface);
        resultClass.addConstructor(CtNewConstructor.defaultConstructor(resultClass));
        resultClass.addField(CtField
                .make(MessageFormat
                                .format(properties.getProperty("clazz.field"),
                                        IObject.class.getName(),
                                        properties.getProperty("field.iobject.name")),
                        resultClass));
        return resultClass;
    }

    /**
     * Adds constructor for wrapper class
     * @param resultClass class for which constructor will be added
     * @param bodyFromMethods constructor's body
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private static void addConstructor(CtClass resultClass, String bodyFromMethods) throws CannotCompileException, NotFoundException {
        resultClass.getDeclaredConstructor(new CtClass[]{}).setBody("{" + bodyFromMethods + "}");
    }

    /**
     * Adds string
     * "this.value = new info.smart_tools.smartactors.core.Field(new info.smart_tools.smartactors.core.FieldName("value"));"
     * or
     * "this.value = new info.smart_tools.smartactors.core.ListField(new info.smart_tools.smartactors.core.FieldName("value"));"
     * in constructor's body
     * @param resultClass observable class
     * @param m method
     * @param constructorBuilder string for constructor body, there appends string
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    private static void handleInterfaceMethod(CtClass resultClass, Method m, StringBuilder constructorBuilder)
            throws CannotCompileException, NotFoundException {

        String fieldNameByMethod = getFieldNameByMethod(m);

        boolean needToCreateField = Arrays.stream(resultClass.getDeclaredFields())
                .noneMatch(val -> val.getName().equals(fieldNameByMethod));
        if (needToCreateField) {
            //TODO: refactor this if-else.
            String fieldClassName;
            if (isListField(m)) {
                fieldClassName = ListField.class.getName();
            } else {
                fieldClassName = Field.class.getName();
            }
            resultClass.addField(CtField
                    .make(MessageFormat
                                    .format(properties.getProperty("clazz.field"),
                                            fieldClassName,
                                            fieldNameByMethod),
                            resultClass));
            constructorBuilder.append("this.").append(fieldNameByMethod).append(" = new ").append(fieldClassName)
                    .append("(new ").append(FieldName.class.getName()).append("(\"").append(getNameByMethod(m)).append("\"));");
        }
        CtMethod ctMethod = CtNewMethod.make(getCtClassMethodString(m), resultClass);
        resultClass.addMethod(ctMethod);
    }

    private static Boolean isListField(Method m) {
        return (m.getName().matches(properties.getProperty("regexp.getter")) && (m.getParameterCount() == 1))
                || m.getName().matches(properties.getProperty("regexp.counter"))
                || (m.getName().matches(properties.getProperty("regexp.setter")) && (m.getParameterTypes()[0].equals(Iterable.class)));
    }

    /**
     * Gives signature of method
     * @param method method
     * @return signature
     */
    private static String getCtClassMethodString(Method method) {
        Class<?> returnType = method.getReturnType();
        String returnTypeName = returnType.getName();
        boolean hasParams = method.getParameterCount() > 0;
        return MessageFormat
                .format(properties.getProperty("method.wrapper"),
                        returnTypeName,
                        method.getName(),
                        hasParams ? getParamsString(method) : "",
                        getCtClassMethodBody(method));
    }

    /**
     * Gives method's body
     * @param method method
     * @return method's body
     */
    private static String getCtClassMethodBody(Method method) {
        Class<?> returnType = method.getReturnType();

        String fieldNameIObject = properties.getProperty("field.iobject.name");
        String fieldNameByMethod = getFieldNameByMethod(method);
        if (method.getName().matches(properties.getProperty("regexp.setter"))) {
            //TODO: refactor this if-else. There are two types of setter: for Field and listField
            if (method.getParameterTypes()[0].equals(Iterable.class)) {
                return MessageFormat
                        .format(properties.getProperty("method.body.setter.list"),
                                fieldNameByMethod,
                                fieldNameIObject,
                                CollectionUtils.class.getName(),
                                getPrimitiveToNewObjectString(method.getParameters()[0].getType()));
            } else {
                return MessageFormat
                        .format(properties.getProperty("method.body.setter"),
                                fieldNameByMethod,
                                fieldNameIObject,
                                getPrimitiveToNewObjectString(method.getParameters()[0].getType()));
            }
        } else if (method.getName().matches(properties.getProperty("regexp.getter"))) {
            return MessageFormat
                    .format(properties.getProperty("method.body.getter"),
                            returnType.getName(),
                            getPrimitiveFromObjectPrefix(returnType) + fieldNameByMethod,
                            fieldNameIObject,
                            primitiveToClassString(returnType) + ".class",
                            method.getParameters().length == 0 ? "" : ".get(arg0)",
                            getPrimitiveFromObjectPostfix(returnType));
        } else if (method.getName().matches(properties.getProperty("regexp.counter"))) {
            return MessageFormat
                    .format(properties.getProperty("method.body.counter"),
                            fieldNameByMethod,
                            fieldNameIObject,
                            "Object.class",
                            ".size()");
        }
        return "";
    }

    /**
     * Add methods from implemented interface {@link IObjectWrapper}
     * @param resultClass observable class
     * @throws NoSuchMethodException
     * @throws CannotCompileException
     * TODO: this code must be rewrote on every {@link IObjectWrapper}'s contract changes
     */
    private static void handleWrapperSpecialMethods(CtClass resultClass) throws NoSuchMethodException, CannotCompileException {

        Method method = IObjectWrapper.class.getMethod("extractWrapped");
        String methodBody = "return " + properties.getProperty("field.iobject.name") + ";";
        String methodStr = MessageFormat
                                .format(properties.getProperty("method.wrapper"),
                                        method.getReturnType().getName(),
                                        method.getName(),
                                        method.getParameterCount() > 0 ? getParamsString(method) : "",
                                        methodBody);
        CtMethod ctMethod = CtNewMethod.make(methodStr, resultClass);
        resultClass.addMethod(ctMethod);

        method = IObjectWrapper.class.getMethod("init", IObject.class);
        methodBody = "this." + properties.getProperty("field.iobject.name") +
                     " = " + method.getParameters()[0].getName() + ";";
        methodStr = MessageFormat
                .format(properties.getProperty("method.wrapper"),
                        method.getReturnType().getName(),
                        method.getName(),
                        method.getParameterCount() > 0 ? getParamsString(method) : "",
                        methodBody);
        ctMethod = CtNewMethod.make(methodStr, resultClass);
        resultClass.addMethod(ctMethod);
    }

    private static String getParamsString(Method m) {
        StringBuilder sb = new StringBuilder();
        for (Parameter p : m.getParameters()) {
            sb.append(p.getType().getName()).append(" ").append(p.getName()).append(" ");
        }
        return sb.toString();
    }

    private static String getPrimitiveFromObjectPrefix(Class<?> clz) {
        String name = primitiveToClassString(clz);
        if (!clz.getName().equals(name)) {
            return "((" + name + ") ";
        }
        return "";
    }

    private static String getPrimitiveFromObjectPostfix(Class<?> clz) {
        return mapPrimitivePostfixes.getOrDefault(clz.getName(), "");
    }

    private static String primitiveToClassString(Class<?> clz) {
        return mapPrimitiveToClass.getOrDefault(clz.getName(), clz.getName());
    }

    //$0 == this, $1 == first argument of method
    private static String getPrimitiveToNewObjectString(Class<?> clz) {
        if (clz.isPrimitive()) {
            return "new " + primitiveToClassString(clz) + "($1)";
        }
        return "$1";
    }

    /**
     * Creates appropriate field name for generated class from interface method name.
     * @param method is method of the interface
     * @return String name of field
     */
    private static String getFieldNameByMethod(Method method) {
        String name = getNameByMethod(method);
        return "_" + name;
    }

    /**
     * Translates given method name to iobject field label.
     * Example:
     * getCounter -> counter
     * setLabel -> label
     * isSomeComplexCondition -> someComplexCondition
     *
     * @param method is method of interface
     * @return String label of iobject field
     */
    private static String getNameByMethod(Method method) {
        String methodName = method.getName();
        String result = methodName.replaceAll(properties.getProperty("regexp.method.to.field.replace"),
                properties.getProperty("regexp.method.to.field.replacement"));
        return result.length() > 1 ? result.substring(0,1).toLowerCase() + result.substring(1)
                : result.toLowerCase();
    }
}
