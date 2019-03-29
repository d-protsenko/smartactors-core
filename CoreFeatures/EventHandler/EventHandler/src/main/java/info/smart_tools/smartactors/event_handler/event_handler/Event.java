package info.smart_tools.smartactors.event_handler.event_handler;

public class Event implements IEvent {

    private static final String UNDEFINED = "undefined";
    private static final Integer EXCEPTION_LEVEL = 4;
    private static final Integer INFO_LEVEL = 2;

    private String type;
    /**
     * The level of event;
     * Example: 0 - trace; 1 - debug; 2 - info; 3 - warning; 4 - error; 5 - fatal;
     */
    private Integer level;
    private String initiator;
    private String message;
    private Object body;
    private Object params;

    private Event() {
    }

    public String getType() {
        return type;
    }

    public Integer getLevel() {
        return level;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getMessage() {
        return message;
    }

    public Object getBody() {
        return body;
    }

    public Object getParams() {
        return params;
    }

    public static Builder builder() {
        return new Event().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder type(final String eventType) {
            Event.this.type = eventType;

            return this;
        }

        public Builder level(final Integer eventLevel) {
            Event.this.level = eventLevel;

            return this;
        }

        public Builder initiator(final String eventInitiator) {
            Event.this.initiator = eventInitiator;

            return this;
        }

        public Builder message(final String eventMessage) {
            Event.this.message = eventMessage;

            return this;
        }

        public Builder body(final Object eventBody) {
            Event.this.body = eventBody;

            return this;
        }

        public Builder params(final Object eventParams) {
            Event.this.params = eventParams;

            return this;
        }

        public IEvent build() {
            if (null == Event.this.type || Event.this.type.isEmpty()) {
                if (Event.this.body != null) {
                    Class bodyClass = Event.this.body.getClass();
                    if (Exception.class.isAssignableFrom(bodyClass)) {
                        Event.this.type = Exception.class.getCanonicalName();
                        Event.this.level = EXCEPTION_LEVEL;
                    } else {
                        Event.this.type = bodyClass.getCanonicalName();
                    }

                } else {
                    Event.this.type = UNDEFINED;
                }
            }
            if (null == Event.this.level) {
                Event.this.level = INFO_LEVEL;
            }
            if (null == Event.this.initiator || Event.this.initiator.isEmpty()) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                Integer number = null;
                if (elements.length > 2) {
                    number = 2;
                } else if (elements.length > 1) {
                    number = 1;
                } else if (elements.length > 0) {
                    number = 0;
                }
                if (number != null) {
                    StackTraceElement element = elements[number];
                    Event.this.initiator = String.format(
                            "%s:%s", element.getClassName(), element.getMethodName()
                    );
                } else {
                    initiator = UNDEFINED;
                }
            }
            if (null == Event.this.message || Event.this.message.isEmpty()) {
                Event.this.message = UNDEFINED;
            }

            return Event.this;
        }
    }

    @Override
    public String toString() {
        if (this.body != null) {
            return this.body.toString();
        }

        if (this.message != null) {
            return message;
        }

        return UNDEFINED;
    }
}
