package info.smart_tools.smartactors.class_management.class_generator_with_java_compile_api.class_builder;

/**
 * Class, fields or methods modifiers
 */
public enum Modifiers {
    /**
     * Modifier - private
     */
    PRIVATE {
        @Override
        public String getValue() {
            return "private";
        }
    },
    /**
     * Modifier - protected
     */
    PROTECTED {
        @Override
        public String getValue() {
            return "protected";
        }
    },
    /**
     * Modifier - public
     */
    PUBLIC {
        @Override
        public String getValue() {
            return "public";
        }
    },
    /**
     * Modifier - class protected
     */
    CLASS_PROTECTED {
        @Override
        public String getValue() {
            return "";
        }
    };

    /**
     * Get string value of modifier
     * @return the modifier value
     */
    public abstract String getValue();
}