package info.smart_tools.smartactors.core.wrapper_generator.class_builder;

/**
 * Class, fields or methods modifiers
 */
public enum Modifiers {
    PRIVATE {
        @Override
        public String getValue() {
            return "private";
        }
    },
    PROTECTED {
        @Override
        public String getValue() {
            return "protected";
        }
    },
    PUBLIC {
        @Override
        public String getValue() {
            return "public";
        }
    },
    CLASS_PROTECTED {
        @Override
        public String getValue() {
            return "";
        }
    };

    public abstract String getValue();
}