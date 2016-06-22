package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Class summary info
 */
public class ClassInfo {

    private ClassBuilder builder;
    private String className;
    private Modifiers classModifier;
    private String inheritableClass;
    private List<String> interfaces = new ArrayList<>();

    public ClassInfo(final ClassBuilder builder) {
        this.builder = builder;
    }

    public ClassInfo setClassModifier(final Modifiers classModifier) {
        this.classModifier = classModifier;

        return this;
    }

    public ClassInfo setClassName(final String className) {
        this.className = className;

        return this;
    }

    public ClassInfo setInheritableClass(final String inheritableClass) {
        this.inheritableClass = inheritableClass;

        return this;
    }

    public ClassInfo setInterfaces(final String interfaceName) {
        this.interfaces.add(interfaceName);

        return this;
    }

    public String getClassName() {
        return className;
    }

    public Modifiers getClassModifier() {
        return classModifier;
    }

    public String getInheritableClass() {
        return inheritableClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public ClassBuilder next() {
        return this.builder;
    }
}