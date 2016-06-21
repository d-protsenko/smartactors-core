package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

/**
 * Parameter summary class
 */
public class ConstructorParameterInfo {

    private ConstructorInfo constructorInfo;
    private String name;
    private String type;

    public ConstructorParameterInfo(final ConstructorInfo constructorInfo) {
        this.constructorInfo = constructorInfo;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ConstructorParameterInfo setName(final String name) {
        this.name = name;

        return this;
    }

    public ConstructorParameterInfo setType(final String type) {
        this.type = type;

        return this;
    }

    public ConstructorInfo next() {
        return this.constructorInfo;
    }
}
