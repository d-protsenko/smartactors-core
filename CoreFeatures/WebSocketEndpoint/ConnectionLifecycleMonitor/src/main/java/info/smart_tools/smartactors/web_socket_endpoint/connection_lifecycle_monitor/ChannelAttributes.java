package info.smart_tools.smartactors.web_socket_endpoint.connection_lifecycle_monitor;

import io.netty.util.AttributeKey;

/**
 *
 */
public enum  ChannelAttributes { ;
    /** Attribute storing identifier of a connection */
    public static final AttributeKey<Object> CONNECTION_ID_ATTRIBUTE = AttributeKey.valueOf("connectionId");
}
