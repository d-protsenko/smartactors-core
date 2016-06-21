package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class builder
 */
public class ClassBuilder {

    private char tabSymbol = '\0';
    private char lineBreakSymbol = '\0';
    private List<String> importsList = new ArrayList<>();
    private String packageName = "";
    private ClassInfo classInfo;
    private List<FieldInfo> fields = new ArrayList<>();
    private List<MethodInfo> methods = new ArrayList<>();
    private List<ConstructorInfo> constructors = new ArrayList<>();

    public ClassBuilder(final char tabSymbol, final char lineBreakSymbol) {
        this.tabSymbol = tabSymbol;
        this.lineBreakSymbol = lineBreakSymbol;
    }

    public ClassBuilder addPackageName(final String packageName) {
        this.packageName = packageName;

        return this;
    }

    public ClassBuilder addImport(final String value) {
        this.importsList.add(value);

        return this;
    }

    public ClassInfo addClass() {
        if (null == this.classInfo) {
            ClassInfo classInfo = new ClassInfo(this);
        }

        return this.classInfo;
    }

    public MethodInfo addMethod() {
        MethodInfo method = new MethodInfo(this);
        this.methods.add(method);

        return method;
    }

    public ConstructorInfo addConstructor() {
        ConstructorInfo constructorInfo = new ConstructorInfo(this);
        this.constructors.add(constructorInfo);

        return constructorInfo;
    }

    public FieldInfo addField() {
        FieldInfo fieldInfo = new FieldInfo(this);
        this.fields.add(fieldInfo);

        return fieldInfo;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public List<ConstructorInfo> getConstructors() {
        return constructors;
    }

    StringBuilder buildClass() {
        StringBuilder builder = new StringBuilder();

        return builder;
    }
}




