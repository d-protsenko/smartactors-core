package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

/**
 * Parameter summary class
 */
public class MethodParameterInfo {

    private MethodInfo methodInfo;
    private String name;
    private String type;

    public MethodParameterInfo(final MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public MethodParameterInfo setName(final String name) {
        this.name = name;

        return this;
    }

    public MethodParameterInfo setType(final String type) {
        this.type = type;

        return this;
    }

    public MethodInfo next() {
        return this.methodInfo;
    }
}
