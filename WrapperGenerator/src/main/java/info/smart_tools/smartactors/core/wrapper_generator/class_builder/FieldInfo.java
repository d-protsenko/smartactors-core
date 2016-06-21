package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

/**
 * Field summary info
 */
public class FieldInfo {

    private ClassBuilder classBuilder;

    private Modifiers modifier;
    private String name;
    private String type;

    public FieldInfo(final ClassBuilder classBuilder) {
        this.classBuilder = classBuilder;
    }

    public Modifiers getModifier() {
        return modifier;
    }

    public FieldInfo setModifier(final Modifiers modifier) {
        this.modifier = modifier;

        return this;
    }

    public String getName() {
        return name;
    }

    public FieldInfo setName(final String name) {
        this.name = name;

        return this;
    }

    public String getType() {
        return type;
    }

    public FieldInfo setType(final String type) {
        this.type = type;

        return this;
    }

    public ClassBuilder next() {
        return this.classBuilder;
    }
}
