package info.smart_tools.smartactors.core.wrapper_generator;

import info.smart_tools.smartactors.core.wrapper_generator.class_builder.ClassBuilder;
import info.smart_tools.smartactors.core.wrapper_generator.class_builder.Modifiers;
import org.junit.Test;

/**
 * Tests for class builde
 */
public class ClassBuilderTest {

    @Test
    public void checkClassBuilder() {
        ClassBuilder cb = new ClassBuilder('\t', '\n');
        cb.addClass()
                .setClassName("A")
                .setInheritableClass("B")
                .setInterfaces("IC")
                .setInterfaces("ID").setClassModifier(Modifiers.PUBLIC)
                .next()
        .addField()
                .setModifier(Modifiers.PRIVATE)
                .setName("field1")
                .setType("String")
                .next()
        .addField()
                .setModifier(Modifiers.PRIVATE)
                .setName("field1")
                .setType("Integer")
                .next()
        .addConstructor()
                .setModifier()
                .setName()
                .
    }
}
