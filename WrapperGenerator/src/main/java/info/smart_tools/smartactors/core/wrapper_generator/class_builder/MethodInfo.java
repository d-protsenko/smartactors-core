package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

import java.util.List;

/**
 * Method summary info
 */
public class MethodInfo {

    private ClassBuilder builder;
    private Modifiers modifier;
    private String name;
    private List<MethodParameterInfo> parameters;
    private List<String> exceptions;
    private String body;

    public MethodInfo(final ClassBuilder builder) {
        this.builder = builder;
    }

    public Modifiers getModifier() {
        return modifier;
    }

    public MethodInfo setModifier(final Modifiers modifier) {
        this.modifier = modifier;

        return this;
    }

    public String getName() {
        return name;
    }

    public MethodInfo setName(final String name) {
        this.name = name;

        return this;
    }

    public List<MethodParameterInfo> getParameters() {
        return parameters;
    }

    public MethodParameterInfo addParameter() {
        return new MethodParameterInfo(this);
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(final String exception) {
        this.exceptions.add(exception);
    }

    public String getBody() {
        return body;
    }

    public MethodInfo setBody(final String body) {
        this.body = body;

        return this;
    }

    public ClassBuilder next() {
        return this.builder;
    }
}
