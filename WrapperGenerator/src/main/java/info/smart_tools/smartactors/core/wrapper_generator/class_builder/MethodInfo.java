package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Method summary info
 */
public class MethodInfo {

    private ClassBuilder builder;
    private Modifiers modifier;
    private String name;
    private List<MethodParameterInfo> parameters = new ArrayList<>();
    private List<String> exceptions = new ArrayList<>();
    private List<String> body = new ArrayList<>();
    private String returnType;

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
        MethodParameterInfo parameterInfo = new MethodParameterInfo(this);
        this.parameters.add(parameterInfo);
        return parameterInfo;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public MethodInfo setExceptions(final String exception) {
        this.exceptions.add(exception);

        return this;
    }

    public List<String> getBody() {
        return body;
    }

    public MethodInfo addStringToBody(final String body) {
        this.body.add(body);

        return this;
    }

    public String getReturnType() {
        return returnType;
    }

    public MethodInfo setReturnType(final String returnType) {
        this.returnType = returnType;

        return this;
    }

    public ClassBuilder next() {
        return this.builder;
    }
}
