# Netty endpoint components feature

This feature contains implementations of components for netty-based endpoints.

## Transports

Netty provides different implementations of channels and event loops called transports. The default transport is based on NIO. There are also available some platform-specific native transport implementations for Linux (uses `epoll`) and Mac/BSD (uses KQueue). There also is a OIO (old IO) locking transport that is not as efficient but still required in some situations.

This feature provides a abstraction over netty transports - interface `INettyTransportProvider` instances of which create objects for specific transport implementation.

The following transport types are registered by default plugin:

* `"nio"` - use NIO transport on any platform
* `"prefer-native"` - use native transport if possible, fallback to NIO
* `"force-native"` - use native transport if possible, fail otherwise
* `"blocking"` - use OIO transport

## Event loops configuration section

This feature adds a configuration section named `"nettyEventLoops"` that may be used to configure event loops used by Netty endpoints to poll channels.

Example of event loop configuration for a server with two (HTTP and HTTPS) server endpoints:

```javascript
{
    "nettyEventLoops": [
        {
            "id": "defaultServerParent",        // Loop group name
            "upcounter": "root",                // Upcounter name
            "transport": "prefer-native",       // Transport type
            "threads": 1                        // Transport-specific parameters
        },
        {
            "id": "defaultServerChild",
            "upcounter": "root",
            "transport": "prefer-native",
            "threads": 4
        },
        
        {
            "id": "httpServerParent",           // Loop group alias
            "alias": "defaultServerParent"
        },
        {
            "id": "httpServerChild",
            "alias": "defaultServerChild"
        },
        
        {
            "id": "httpsServerParent",
            "alias": "defaultServerParent"
        },
        {
            "id": "httpsServerChild",
            "alias": "defaultServerChild"
        }
    ]
}
```

Aliases are defined to make it possible to change loop groups used by endpoint without editing endpoint configuration. This is necessary to provide ability to tune endpoint performance independently from endpoint logic and (probably) define different configurations for different server instances.

Event loop groups described in `"nettyEventLoops"` section are created lazily (group is created when a first endpoint requiring a group is created). Until the group is created the group configuration may be overwritten by another configuration (such behaviour makes it possible to define some default group configuration in feature containing endpoint type definition and override it in some other feature that contains a endpoint of defined type or in a feature that loads before any feature containing endpoint of defined type). Configuration trying to overwrite configuration of already created group will cause exception.
